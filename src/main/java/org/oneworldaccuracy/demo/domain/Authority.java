package org.oneworldaccuracy.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * An authority (a security role) used by Spring Security.
 */
@Entity
@Table(name = "authority")
@Data
@NoArgsConstructor
public class Authority implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Size(max = 50)
    @Column(length = 50)
    private String name;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "user_authority",
        joinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")},
        inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @BatchSize(size = 20)
    private Set<User> users = new HashSet<>();

    public Authority(String name) {
        this.name = name;
    }
}
