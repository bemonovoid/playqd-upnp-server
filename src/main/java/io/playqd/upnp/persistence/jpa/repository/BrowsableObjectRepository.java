package io.playqd.upnp.persistence.jpa.repository;

import io.playqd.upnp.exception.DatabaseEntityNotFoundException;
import io.playqd.upnp.persistence.jpa.entity.BrowsableObjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.Optional;

public interface BrowsableObjectRepository extends IdentityJpaRepository<BrowsableObjectEntity> {

    /**
     *
     * @param objectId
     * @return found entity if exists, otherwise throws an exception
     * @throws DatabaseEntityNotFoundException
     */
    default BrowsableObjectEntity getByObjectId(String objectId) {
        return findByObjectId(objectId).orElseThrow(() -> new DatabaseEntityNotFoundException(
                String.format("Browsable object with objectId '%s' was not found.", objectId)));
    }

    Optional<BrowsableObjectEntity> findByObjectId(String objectId);

    Optional<BrowsableObjectEntity> findFirstByParentId(long parentId);

    long countByParentId(long parentId);

    List<BrowsableObjectEntity> findByParentIsNull();

    Page<BrowsableObjectEntity> findAllByParentId(long parentId, Pageable pageable);

    Window<BrowsableObjectEntity> findAllByParentIdOrderByUpnpClassDescDcTitleAsc(long parentId,
                                                                                  ScrollPosition scrollPosition);
}