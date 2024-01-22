package mz.org.fgh.mentoring.entity.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import mz.org.fgh.mentoring.base.BaseEntity;
import mz.org.fgh.mentoring.entity.employee.Employee;
import mz.org.fgh.mentoring.entity.healthfacility.HealthFacility;
import mz.org.fgh.mentoring.entity.partner.Partner;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "Location")
@Table(name = "location")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Location extends BaseEntity {
    public static final String LOCATION_LEVEL_NATIONAL = "NATIONAL";
    public static final String LOCATION_LEVEL_PROVINCIAL = "PROVINCIAL";
    public static final String LOCATION_LEVEL_DISTRITAL = "DISTRITAL";
    public static final String LOCATION_LEVEL_HEALTH_FACILITY = "HEALTH_FACILITY";


    @ManyToOne
    @JoinColumn(name="EMPLOYEE_ID", nullable=false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROVINCE_ID")
    private Province province;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DISTRICT_ID")
    private District district;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "HEALTH_FACILITY_ID")
    private HealthFacility healthFacility;

    @Column(name = "LOCATION_LEVEL", nullable = false, length = 80)
    private String locationLevel;



    private void determineLocationLevel() {
        if (this.healthFacility != null) {
            this.locationLevel = LOCATION_LEVEL_HEALTH_FACILITY;
        } else if (this.district != null) {
            this.locationLevel = LOCATION_LEVEL_DISTRITAL;
        } else if (this.province != null) {
            this.locationLevel = LOCATION_LEVEL_PROVINCIAL;
        } else {
            this.locationLevel = LOCATION_LEVEL_NATIONAL;
        }
    }

    private boolean isHealthFacilityLevel() {
        return this.locationLevel.equals(LOCATION_LEVEL_HEALTH_FACILITY);
    }

    private boolean isProvincialLevel() {
        return this.locationLevel.equals(LOCATION_LEVEL_PROVINCIAL);
    }

    private boolean isDistrictalLevel() {
        return this.locationLevel.equals(LOCATION_LEVEL_DISTRITAL);
    }

}
