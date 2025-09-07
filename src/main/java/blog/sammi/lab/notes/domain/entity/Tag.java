package blog.sammi.lab.notes.domain.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@ToString(exclude = {"user", "notes"})
@EqualsAndHashCode(callSuper = true)
public class Tag extends Auditable {
    @Column(nullable = false)
    private String name;
    
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "tags")
    private Set<Note> notes = new HashSet<>();
}
