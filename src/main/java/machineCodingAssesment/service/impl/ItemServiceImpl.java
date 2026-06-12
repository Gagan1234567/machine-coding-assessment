package machineCodingAssesment.service.impl;

import lombok.RequiredArgsConstructor;
import machineCodingAssesment.dto.response.ItemResponse;
import machineCodingAssesment.exception.ResourceNotFoundException;
import machineCodingAssesment.model.Item;
import machineCodingAssesment.repository.ItemRepository;
import machineCodingAssesment.service.ItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public List<ItemResponse> getAll() {
        return itemRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponse getById(String id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return toResponse(item);
    }

    private ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }
}
