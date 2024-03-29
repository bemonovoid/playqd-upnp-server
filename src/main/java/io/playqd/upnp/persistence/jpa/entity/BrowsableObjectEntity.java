package io.playqd.upnp.persistence.jpa.entity;

import io.playqd.upnp.persistence.jpa.dao.BrowsableObjectSetter;
import io.playqd.upnp.service.contentdirectory.UpnpClass;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = "location", callSuper = false)
@Entity
@Table(name = BrowsableObjectEntity.TABLE_NAME,  indexes = {
        @Index(name = "object_id_idx", columnList = "object_id")
})
public class BrowsableObjectEntity extends PersistableAuditableEntity implements BrowsableObjectSetter {

    public static final String TABLE_NAME = "upnp_browsable_object";

    private static final String COL_OBJECT_ID = "object_id";
    private static final String COL_TITLE = "title";
    private static final String COL_LOCATION = "location";
    private static final String COL_UPNP_CLASS = "upnp_class";
    private static final String COL_MIME_TYPE = "mime_type";
    private static final String COL_SIZE = "size";
    private static final String COL_PARENT_ID = "parent_id";
    private static final String COL_CHILD_COUNT = "child_count";
    private static final String COL_CHILD_CONTAINER_COUNT = "child_container_count";

    @Column(name = COL_OBJECT_ID, nullable = false, unique = true)
    private String objectId;

    @Column(name = COL_TITLE, nullable = false)
    private String dcTitle;

    @Column(name = COL_LOCATION, nullable = false, unique = true)
    private String location;

    @Column(name = COL_UPNP_CLASS, nullable = false)
    @Enumerated(EnumType.STRING)
    private UpnpClass upnpClass;

    @Column(name = COL_MIME_TYPE)
    private String mimeType;

    @Column(name = COL_SIZE)
    private Long size;

    @Column(name = COL_CHILD_COUNT)
    private long childCount;

    @Column(name = COL_CHILD_CONTAINER_COUNT)
    private long childContainerCount;

    @Column(name = COL_PARENT_ID, insertable=false, updatable=false)
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    private BrowsableObjectEntity parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BrowsableObjectEntity> children;

    public final long getChildCountAvailable() {
        return !CollectionUtils.isEmpty(getChildren()) ? getChildren().size() : getChildCount();
    }

}
