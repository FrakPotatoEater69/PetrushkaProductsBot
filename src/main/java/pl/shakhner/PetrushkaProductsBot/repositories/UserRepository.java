package pl.shakhner.PetrushkaProductsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.shakhner.PetrushkaProductsBot.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
