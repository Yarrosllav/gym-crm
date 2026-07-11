package com.gym.dao;

import com.gym.dao.impl.TrainerDAOImpl;
import com.gym.db.Storage;
import com.gym.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TrainerDAOTest {

    private TrainerDAO trainerDAO;

    private Storage storage;

    private Map<UUID, Trainer> trainersMap;

    @BeforeEach
    void setUp() {
        trainersMap = new HashMap<>();
        storage = new Storage(new HashMap<>(), new HashMap<>(), trainersMap);
        trainerDAO = new TrainerDAOImpl(storage);
    }

    @Test
    void testCreate_ShouldAddTrainersToMapAndGenerateId() {
        var trainer = new Trainer();
        trainer.setFirstName("John");
        trainer.setLastName("Doe");

        trainerDAO.create(trainer);

        assertEquals(1, trainersMap.size());

        assertNotNull(trainer.getUserId());

        var createdTrainer = trainersMap.get(trainer.getUserId());
        assertEquals("John", createdTrainer.getFirstName());
    }

    @Test
    void testFindById_ShouldReturnTrainer() {
        var id = UUID.randomUUID();
        var trainer = new Trainer();
        trainer.setUserId(id);
        trainer.setFirstName("Jane");
        trainersMap.put(id, trainer);

        var result = trainerDAO.findById(id);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
    }


    @Test
    void testUpdate_ShouldOverwriteExistingTrainer() {
        var id = UUID.randomUUID();
        var oldTrainer = new Trainer();
        oldTrainer.setUserId(id);
        oldTrainer.setFirstName("OldName");
        trainersMap.put(id, oldTrainer);

        var updatedTrainer = new Trainer();
        updatedTrainer.setUserId(id);
        updatedTrainer.setFirstName("NewName");

        trainerDAO.update(updatedTrainer);

        assertEquals(1, trainersMap.size());
        assertEquals("NewName", trainersMap.get(id).getFirstName());
    }
}
