package com.gym.dao;

import com.gym.dao.impl.TraineeDAOImpl;
import com.gym.db.Storage;
import com.gym.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDAOTest {

    private TraineeDAO traineeDAO;

    private Storage storage;

    private Map<UUID, Trainee> traineesMap;

    @BeforeEach
    void setUp() {
        traineesMap = new HashMap<>();
        storage = new Storage(traineesMap, new HashMap<>(), new HashMap<>());
        traineeDAO = new TraineeDAOImpl(storage);
    }

    @Test
    void testCreate_ShouldAddTraineeToMapAndGenerateId() {
        var trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        traineeDAO.create(trainee);

        assertEquals(1, traineesMap.size());

        assertNotNull(trainee.getUserId());

        var createdTrainee = traineesMap.get(trainee.getUserId());
        assertEquals("John", createdTrainee.getFirstName());
    }

    @Test
    void testFindById_ShouldReturnTrainee() {
        var id = UUID.randomUUID();
        var trainee = new Trainee();
        trainee.setUserId(id);
        trainee.setFirstName("Jane");
        traineesMap.put(id, trainee);

        var result = traineeDAO.findById(id);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void testDelete_ShouldRemoveTraineeFromMap() {
        var id = UUID.randomUUID();
        var trainee = new Trainee();
        trainee.setUserId(id);
        traineesMap.put(id, trainee);

        assertEquals(1, traineesMap.size());

        traineeDAO.delete(id);

        assertTrue(traineesMap.isEmpty());
        assertNull(traineesMap.get(id));
    }

    @Test
    void testUpdate_ShouldOverwriteExistingTrainee() {
        var id = UUID.randomUUID();
        var oldTrainee = new Trainee();
        oldTrainee.setUserId(id);
        oldTrainee.setFirstName("OldName");
        traineesMap.put(id, oldTrainee);

        var updatedTrainee = new Trainee();
        updatedTrainee.setUserId(id);
        updatedTrainee.setFirstName("NewName");

        traineeDAO.update(updatedTrainee);

        assertEquals(1, traineesMap.size());
        assertEquals("NewName", traineesMap.get(id).getFirstName());
    }
}
