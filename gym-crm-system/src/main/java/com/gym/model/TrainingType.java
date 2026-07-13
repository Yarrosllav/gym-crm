package com.gym.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "training_types")
@Immutable
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true, updatable = false)
    private String trainingTypeName;
}
