package pl.shakhner.PetrushkaProductsBot.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.shakhner.PetrushkaProductsBot.botAPI.ItemType;
import pl.shakhner.PetrushkaProductsBot.dto.ItemPriceTitleDTO;
import pl.shakhner.PetrushkaProductsBot.models.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByType(ItemType itemType);

    List<Item> findAllByType(ItemType itemType, Pageable pageable);

    @Query("SELECT new pl.shakhner.PetrushkaProductsBot.dto.ItemPriceTitleDTO(i.id, i.price, i.title) FROM Item i WHERE i.id IN :itemIds")
    List<ItemPriceTitleDTO> findPricesTitlesById(List<Long> itemIds);
}
