package machineCodingAssesment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import machineCodingAssesment.model.Item;
import machineCodingAssesment.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the fixed, preconfigured item catalog at startup (rule 2).
 * Items use natural-key codes as ids so client references stay stable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ItemCatalogSeeder implements CommandLineRunner {

    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        List<Item> catalog = List.of(
                Item.builder().id("DOCUMENT").name("Document").build(),
                Item.builder().id("ELECTRONICS").name("Electronics").build(),
                Item.builder().id("GROCERY").name("Grocery").build(),
                Item.builder().id("CLOTHING").name("Clothing").build(),
                Item.builder().id("FOOD").name("Food").build(),
                Item.builder().id("FURNITURE").name("Furniture").build()
        );
        catalog.forEach(itemRepository::save);
        log.info("Seeded {} preconfigured items into the catalog", catalog.size());
    }
}
