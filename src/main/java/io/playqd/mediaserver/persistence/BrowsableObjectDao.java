package io.playqd.mediaserver.persistence;

import io.playqd.mediaserver.persistence.jpa.dao.BrowsableObjectSetter;
import io.playqd.mediaserver.persistence.jpa.dao.PersistedBrowsableObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface BrowsableObjectDao {

  long countChildren(long parentId);

  boolean hasChildren(long parentId);

  PersistedBrowsableObject getOne(long id);

  PersistedBrowsableObject getChildRoot(PersistedBrowsableObject childObject);

  Optional<PersistedBrowsableObject> getOneByObjectId(String objectId);

  List<PersistedBrowsableObject> getRoots();

  Page<PersistedBrowsableObject> getChildren(long parentId, PageRequest request);

  PersistedBrowsableObject save(Consumer<BrowsableObjectSetter> consumer);

  PersistedBrowsableObject save(PersistedBrowsableObject parent,
                                List<Consumer<BrowsableObjectSetter>> children);
}