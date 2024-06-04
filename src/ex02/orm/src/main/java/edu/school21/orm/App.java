package edu.school21.orm;

import edu.school21.orm.entity.User;
import edu.school21.orm.repository.OrmManager;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        OrmManager manager = new OrmManager();

        manager.save(new User("maxim", "ilenkov", 22));
        manager.save(new User("vanya", "poncho", 42));
        manager.save(new User("lena", "praga", 55));


        User u1 = manager.findById(3L, User.class);
        System.out.println("Получение юзера с айди 3: " + u1);
        User u2 = manager.findById(1L, User.class);
        System.out.println("Получение юзера с айди 1: " + u2);
        User u3 = manager.findById(2L, User.class);
        System.out.println("Получение юзера с айди 2: " + u3);

        u2.setFirstName("max");
        manager.update(u2);
        System.out.println("Обновленный юзер из таблицы с айди 1 " + manager.findById(1L, User.class));
        System.out.println("Обновленный юзер из java code " + u2);
    }
}
