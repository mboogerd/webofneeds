package won.protocol.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import won.protocol.model.Need;
import won.protocol.model.NeedState;

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * User: Gabriel
 * Date: 02.11.12
 * Time: 15:28
 */

public interface NeedRepository extends WonRepository<Need> {

  List<Need> findByNeedURI(URI URI);

  @Query("select needURI from Need")
  List<URI> getAllNeedURIs();

  @Query("select needURI from Need need")
  Slice<URI> getAllNeedURIs(Pageable pageable);

  @Query("select needURI from Need need where need.state = :needState")
  Slice<URI> getAllNeedURIs(@Param("needState") NeedState needState, Pageable pageable);

  Need findOneByNeedURI(URI needURI);

  @Query("select needURI from Need need where need.creationDate < :referenceDate")
  Slice<URI> getNeedURIsBefore(@Param("referenceDate") Date referenceDate, Pageable pageable);

  @Query("select needURI from Need need where need.creationDate < :referenceDate and need.state = :needState")
  Slice<URI> getNeedURIsBefore(@Param("referenceDate") Date referenceDate,
                               @Param("needState") NeedState needState,
                               Pageable pageable);

  @Query("select needURI from Need need where need.creationDate > :referenceDate")
  Slice<URI> getNeedURIsAfter(@Param("referenceDate") Date referenceDate, Pageable pageable);

  @Query("select needURI from Need need where need.creationDate > :referenceDate and need.state = :needState")
  Slice<URI> getNeedURIsAfter(@Param("referenceDate") Date referenceDate,
                              @Param("needState") NeedState needState,
                              Pageable pageable);
}
