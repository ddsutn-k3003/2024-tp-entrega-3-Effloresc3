package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Vianda;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViandaRepository {
  private final EntityManager entityManager;

  public ViandaRepository(final EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Vianda save(final Vianda vianda) throws NoSuchElementException {
    if (Objects.isNull(vianda.getId())) {
      entityManager.getTransaction().begin();
      entityManager.persist(vianda);
      entityManager.getTransaction().commit();
    }
    return vianda;
  }

  public Vianda findByQr(String qr) {
    TypedQuery<Vianda> query = entityManager.createQuery(
        "SELECT v FROM Vianda v WHERE v.qr = :qr", Vianda.class);
    query.setParameter("qr", qr);
    return query.getSingleResult();
  }

  public List<Vianda> findByCollaboratorIdAndYearAndMonth(
      Long colaboradorId, Integer mes, Integer anio) {
    YearMonth yearMonth = YearMonth.of(anio, mes);
    LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
    LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

    TypedQuery<Vianda> query = entityManager.createQuery(
        "SELECT v FROM Vianda v WHERE v.colaboradorId = :colaboradorId " +
            "AND v.fechaElaboracion >= :startOfMonth AND v.fechaElaboracion <= :endOfMonth",
        Vianda.class);
    query.setParameter("colaboradorId", colaboradorId);
    query.setParameter("startOfMonth", startOfMonth);
    query.setParameter("endOfMonth", endOfMonth);

    return query.getResultList();
  }


  public void clearDB() {
    entityManager.getTransaction().begin();
    try {
      entityManager.createQuery("DELETE FROM Vianda").executeUpdate();
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      entityManager.getTransaction().rollback();
      throw e;
    }
  }
}