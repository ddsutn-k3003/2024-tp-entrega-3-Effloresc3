package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Vianda;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.exception.ConstraintViolationException;

@Getter
@Setter
public class ViandaRepository {
  private final EntityManager entityManager;

  public ViandaRepository(final EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Vianda save(final Vianda vianda)
      throws NoSuchElementException, ConstraintViolationException {
    EntityTransaction transaction = null;
    try {
      if (Objects.isNull(vianda.getId())) {
        transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(vianda);
        transaction.commit();
      }
    } catch (PersistenceException pe) {
      if (pe.getCause() instanceof ConstraintViolationException) {
        if (Objects.nonNull(transaction)) {
          transaction.rollback();
        }
        throw new RuntimeException("Ya existe una vianda con el mismo codigo QR");
      }
    } finally {
      if (Objects.nonNull(transaction) && transaction.isActive()) {
        transaction.rollback();
      }
    }
    return vianda;
  }

  public Vianda findByQr(String qr) {
    TypedQuery<Vianda> query =
        entityManager.createQuery("SELECT v FROM Vianda v WHERE v.qr = :qr", Vianda.class);
    query.setParameter("qr", qr);
    return query.getSingleResult();
  }

  public List<Vianda> findByCollaboratorIdAndYearAndMonth(
      Long colaboradorId,
      Integer mes,
      Integer anio
  ) {
    YearMonth yearMonth = YearMonth.of(anio, mes);
    LocalDateTime startOfMonth = yearMonth.atDay(1)
        .atStartOfDay();
    LocalDateTime endOfMonth = yearMonth.atEndOfMonth()
        .atTime(LocalTime.MAX);

    TypedQuery<Vianda> query = entityManager.createQuery(
        "SELECT v FROM Vianda v WHERE v.colaboradorId = :colaboradorId "
            + "AND v.fechaElaboracion >= :startOfMonth AND v.fechaElaboracion <= :endOfMonth",
        Vianda.class
    );
    query.setParameter("colaboradorId", colaboradorId);
    query.setParameter("startOfMonth", startOfMonth);
    query.setParameter("endOfMonth", endOfMonth);

    return query.getResultList();
  }

  public void clearDB() {
    entityManager.getTransaction()
        .begin();
    try {
      entityManager.createQuery("DELETE FROM Vianda")
          .executeUpdate();
      entityManager.getTransaction()
          .commit();
    } catch (Exception e) {
      entityManager.getTransaction()
          .rollback();
      throw e;
    }
  }
}