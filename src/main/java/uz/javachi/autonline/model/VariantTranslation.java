package uz.javachi.autonline.model;

import jakarta.persistence.*;
import lombok.*;
import uz.javachi.autonline.config.Localized;

@Entity
@Table(name = "variant_translation"/*, indexes = {
        @Index(name = "idx_variant_lang", columnList = "variant_id, lang")
}*/)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantTranslation implements Localized {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 10, nullable = false)
    private String lang;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private Variant variant;

    @Override
    public String toString() {
        return "VariantTranslation{" +
                "id=" + id +
                ", lang='" + lang + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
