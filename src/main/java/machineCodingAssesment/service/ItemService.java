package machineCodingAssesment.service;

import machineCodingAssesment.dto.response.ItemResponse;

import java.util.List;

public interface ItemService {
    List<ItemResponse> getAll();
    ItemResponse getById(String id);
}
