package mz.org.fgh.mentoring.repository.form;

import io.micronaut.data.annotation.Repository;
import mz.org.fgh.mentoring.base.AbstaractBaseRepository;
import mz.org.fgh.mentoring.entity.form.Form;
import mz.org.fgh.mentoring.entity.tutor.Tutor;
import org.hibernate.SessionFactory;

import org.hibernate.query.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Jose Julai Ritsure
 */
@Repository
public abstract class AbstractFormRepository extends AbstaractBaseRepository implements FormRepository {

    private final SessionFactory session;

    protected AbstractFormRepository(SessionFactory session) {
        this.session = session;
    }



    @Transactional
    @Override
    public List<Form> search(final String code, final String name, final String programmaticArea) {

        String sql = "SELECT DISTINCT(f.id) FROM forms f " +
                " INNER JOIN form_type ft ON f.FORM_TYPE_ID = ft.ID " +
                " INNER JOIN partners p ON f.PARTNER_ID = p.ID " +
                " INNER JOIN programmatic_areas pa ON f.PROGRAMMATIC_AREA_ID = pa.ID ";

        if(code != null || name != null || programmaticArea != null) {
            sql += " WHERE 1=1 ";
        }
        if (code != null) {
            sql += " AND f.code like '%" + code + "%' ";
        }
        if (name != null) {
            sql += " AND f.name like '%" + name + "%' ";
        }
        if (programmaticArea != null) {
            sql += " AND pa.description like '%" + programmaticArea + "%' ";
        }

        //sql += addUserAuthCondition(user);

        Query qw = this.session.getCurrentSession().createSQLQuery(sql);
        //sqlQuery.setParameter("startDate", user.getId());
        List<Long> ids = qw.getResultList();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Form> criteria = builder.createQuery(Form.class);
        Root<Form> root = criteria.from(Form.class);
        criteria.select(root).where(root.get("id").in(ids));
        Query q = this.session.getCurrentSession().createQuery(criteria);
        List<Form> forms = q.getResultList();
        return forms;
    }

    @Override
    public List<Form> getAllOfTutor(Tutor tutor) {
        // Define the HQL query to fetch forms based on the tutor ID
        String hql = "select f from Form f " +
                "join f.programmaticArea pa " +
                "join pa.tutorProgrammaticAreas tpa " +
                "join tpa.tutor t " +
                "where t.id = :tutorId";

        // Create the query and set the parameter for tutor ID
        Query<Form> query = this.session.openSession().createQuery(hql, Form.class);
        query.setParameter("tutorId", tutor.getId());

        // Execute the query and retrieve the list of forms
        List<Form> forms = query.getResultList();

        return forms;
    }

}
