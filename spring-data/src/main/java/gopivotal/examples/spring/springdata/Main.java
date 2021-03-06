/**
 * Copyright GoPivotal, Inc.
 */
package gopivotal.examples.spring.springdata;

import static org.springframework.data.jpa.domain.Specifications.where;
import gopivotal.examples.spring.springdata.gemfire.Post;
import gopivotal.examples.spring.springdata.gemfire.PostRepository;
import gopivotal.examples.spring.springdata.jpa.Person;
import gopivotal.examples.spring.springdata.jpa.PersonRepository;
import gopivotal.examples.spring.utils.IdGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.jpa.domain.Specification;

/**
 * Small driver to bootstrap the example
 * 
 * @author cdelashmutt
 */
public class Main
{

	private static Logger log = Logger.getLogger(Main.class);

	/**
	 * @param args
	 */
	public static void main(String[] args)
		throws IOException
	{
		@SuppressWarnings("resource")
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:META-INF/spring/*-context.xml");

		context.registerShutdownHook();

		PersonRepository personRepository = context.getBean("personRepository",
				PersonRepository.class);

		PostRepository postRepository = context.getBean("postRepository", PostRepository.class);

		@SuppressWarnings("unchecked")
		IdGenerator<Integer> idGenerator = (IdGenerator<Integer>)context.getBean("postIdGenerator");

		System.out.println("----------------------------");
		System.out.println("---- Spring Data Example----");
		System.out.println("----------------------------\n");

		BufferedReader input = new BufferedReader(new InputStreamReader(
				System.in));
		boolean keepGoing = true;
		while (keepGoing)
		{
			System.out.println("----------------------------");
			System.out.println("1. Lookup Person by Id");
			System.out.println("2. Find active people");
			System.out.println("3. Find people by Last Name starting with...");
			System.out
					.println("4. Find active people by First Name containing...");
			System.out
					.println("5. Find active people by First Name containing (Specification)...");
			System.out.println("6. Create Post for user");
			System.out.println("7. Get Posts for user id");
			System.out.println("99. Quit");
			System.out.println("----------------------------");
			System.out.print(">");

			String command = input.readLine();

			if (command.equals("99"))
			{
				keepGoing = false;
			}
			else if (command.equals("1"))
			{
				System.out.print("Enter id: ");
				try
				{
					int id = Integer.parseInt(input.readLine());
					System.out.println(personRepository.findOne(id));
				}
				catch (Exception e)
				{
					log.error("Error retrieving person", e);
				}
			}
			else if (command.equals("2"))
			{
				System.out.print("Enter active flag: ");
				try
				{
					boolean active = Boolean.parseBoolean(input.readLine());
					for (Person person : personRepository.findByActive(active))
					{
						System.out.println(person);
					}
				}
				catch (Exception e)
				{
					log.error("Error retrieving person", e);
				}
			}
			else if (command.equals("3"))
			{
				System.out.print("Enter starting characters: ");
				try
				{
					String startingWith = input.readLine();
					for (Person person : personRepository
							.findByLastNameStartingWithOrderByFirstNameAsc(startingWith))
					{
						System.out.println(person);
					}
				}
				catch (Exception e)
				{
					log.error("Error retrieving person", e);
				}
			}
			else if (command.equals("4"))
			{
				System.out.print("Enter first name characters: ");
				try
				{
					String containing = input.readLine();
					for (Person person : personRepository
							.findActiveByFirstName(containing))
					{
						System.out.println(person);
					}
				}
				catch (Exception e)
				{
					log.error("Error retrieving person", e);
				}
			}
			else if (command.equals("5"))
			{
				System.out.print("Enter first name characters: ");
				try
				{
					String containing = input.readLine();
					List<Person> people = personRepository.findAll(where(isActivePerson()).and(firstNameContains(containing)));
					for (Person person : people)
					{
						System.out.println(person);
					}
				}
				catch (Exception e)
				{
					log.error("Error retrieving person", e);
				}
			}
			else if (command.equals("6"))
			{
				try
				{
					System.out.print("Enter User id: ");
					int userId = Integer.parseInt(input.readLine());
					System.out.print("Enter post text: ");
					String postText = input.readLine();
					
					Post post = new Post(idGenerator.generate(), userId, postText);

					log.trace("Saving post: " + post);
					postRepository.save(post);
				}
				catch (Exception e)
				{
					log.error("Error saving post", e);
				}
			}
			else if (command.equals("7"))
			{
				try
				{
					System.out.print("Enter User id: ");
					int userId = Integer.parseInt(input.readLine());

					for(Post post : postRepository.findByUserId(userId))
					{
						System.out.println(post);
					}
				}
				catch (Exception e)
				{
					log.error("Error getting posts", e);
				}
			}
			else
			{
				System.out.println("Bad command.");
			}
		}
	}

	public static Specification<Person> isActivePerson()
	{
		return new Specification<Person>()
		{
			public Predicate toPredicate(Root<Person> root,
					CriteriaQuery<?> query, CriteriaBuilder builder)
			{

				return builder.equal(root.get("active"), true);
			}
		};
	}

	public static Specification<Person> firstNameContains(final String contains)
	{
		return new Specification<Person>()
		{
			public Predicate toPredicate(Root<Person> root,
					CriteriaQuery<?> query, CriteriaBuilder builder)
			{

				return builder.like(root.<String>get("firstName"), "%" + contains + "%");
			}
		};
	}
}
