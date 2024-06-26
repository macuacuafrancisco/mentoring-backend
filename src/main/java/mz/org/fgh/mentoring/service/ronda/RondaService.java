package mz.org.fgh.mentoring.service.ronda;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Singleton;
import mz.org.fgh.mentoring.dto.ronda.RondaDTO;
import mz.org.fgh.mentoring.entity.healthfacility.HealthFacility;
import mz.org.fgh.mentoring.entity.ronda.Ronda;
import mz.org.fgh.mentoring.entity.ronda.RondaMentee;
import mz.org.fgh.mentoring.entity.ronda.RondaMentor;
import mz.org.fgh.mentoring.entity.ronda.RondaType;
import mz.org.fgh.mentoring.entity.tutor.Tutor;
import mz.org.fgh.mentoring.entity.tutored.Tutored;
import mz.org.fgh.mentoring.entity.user.User;
import mz.org.fgh.mentoring.repository.healthFacility.HealthFacilityRepository;
import mz.org.fgh.mentoring.repository.ronda.RondaMenteeRepository;
import mz.org.fgh.mentoring.repository.ronda.RondaMentorRepository;
import mz.org.fgh.mentoring.repository.ronda.RondaRepository;
import mz.org.fgh.mentoring.repository.ronda.RondaTypeRepository;
import mz.org.fgh.mentoring.repository.tutor.TutorRepository;
import mz.org.fgh.mentoring.repository.tutored.TutoredRepository;
import mz.org.fgh.mentoring.repository.user.UserRepository;
import mz.org.fgh.mentoring.util.DateUtils;
import mz.org.fgh.mentoring.util.LifeCycleStatus;
import mz.org.fgh.mentoring.util.Utilities;

@Singleton
public class RondaService {

    private final RondaRepository rondaRepository;
    private final RondaMentorRepository rondaMentorRepository;
    private final RondaMenteeRepository rondaMenteeRepository;
    private final UserRepository userRepository;
    private final TutoredRepository tutoredRepository;
    private final TutorRepository tutorRepository;
    private final HealthFacilityRepository healthFacilityRepository;
    private final RondaTypeRepository rondaTypeRepository;

    public RondaService(RondaRepository rondaRepository, RondaMentorRepository rondaMentorRepository,
                        RondaMenteeRepository rondaMenteeRepository, UserRepository userRepository,
                        TutoredRepository tutoredRepository, TutorRepository tutorRepository,
                        HealthFacilityRepository healthFacilityRepository, RondaTypeRepository rondaTypeRepository) {
        this.rondaRepository = rondaRepository;
        this.rondaMentorRepository = rondaMentorRepository;
        this.rondaMenteeRepository = rondaMenteeRepository;
        this.userRepository = userRepository;
        this.tutoredRepository = tutoredRepository;
        this.tutorRepository = tutorRepository;
        this.healthFacilityRepository = healthFacilityRepository;
        this.rondaTypeRepository = rondaTypeRepository;
    }

    public List<Ronda> findAll(){
        return this.rondaRepository.findAll();
    }

    public Optional<Ronda> findById(final Long id){
        return this.rondaRepository.findById(id);
    }

    public Optional<Ronda> findByUuid(final String uuid){
      return this.rondaRepository.findByUuid(uuid);
    }

    public List<Ronda> findRondaWithLimit(long limit, long offset){
        return this.rondaRepository.findRondaWithLimit(limit, offset);
    }

    public Ronda createRonda(final Ronda ronda, Long userId){
        User user = userRepository.findById(userId).get();
        ronda.setCreatedBy(user.getUuid());
        ronda.setCreatedAt(DateUtils.getCurrentDate());
        ronda.setLifeCycleStatus(LifeCycleStatus.ACTIVE);
        return this.rondaRepository.save(ronda);
    }

    public List<RondaDTO> getAllRondasOfMentor(Long mentorId) {
        List<Ronda> rondaList = this.rondaRepository.getAllRondasOfMentor(mentorId, LifeCycleStatus.BLOCKED);
        List<RondaDTO> rondas = new ArrayList<>();
        for (Ronda ronda: rondaList) {
            RondaDTO rondaDTO = new RondaDTO(ronda);
            rondas.add(rondaDTO);
        }
        return rondas;
    }

    public List<RondaDTO> createRondas(List<RondaDTO> rondaDTOS, Long userId) {
        List<RondaDTO> rondas = new ArrayList<>();
        for (RondaDTO rondaDTO: rondaDTOS) {
            RondaDTO dto = this.createRonda(rondaDTO, userId);
            rondas.add(dto);
        }
      return rondas;
    }

    public RondaDTO createRonda(RondaDTO rondaDTO, Long userId) {
        Ronda ronda = rondaDTO.getRonda();
        User user = userRepository.findById(userId).get();
        HealthFacility healthFacility = healthFacilityRepository.findByUuid(ronda.getHealthFacility().getUuid()).get();
        Tutor mentor = tutorRepository.findByUuid(ronda.getRondaMentors().get(0).getMentor().getUuid()).get();
        RondaType rondaType = rondaTypeRepository.findByUuid(ronda.getRondaType().getUuid()).get();
        ronda.setCreatedBy(user.getUuid());
        ronda.setCreatedAt(DateUtils.getCurrentDate());
        ronda.setLifeCycleStatus(LifeCycleStatus.ACTIVE);
        ronda.setHealthFacility(healthFacility);
        ronda.setRondaType(rondaType);
        Ronda createdRonda = this.rondaRepository.save(ronda);
        if(Utilities.listHasElements(rondaDTO.getRondaMentors())) {
            List<RondaMentor> rondaMentors = ronda.getRondaMentors();
            List<RondaMentor> savedRondaMentors = new ArrayList<>();
            for (RondaMentor rondaMentor: rondaMentors) {
                rondaMentor.setRonda(createdRonda);
                rondaMentor.setMentor(mentor);
                rondaMentor.setCreatedBy(user.getUuid());
                rondaMentor.setCreatedAt(DateUtils.getCurrentDate());
                rondaMentor.setLifeCycleStatus(LifeCycleStatus.ACTIVE);
                RondaMentor saveRondaMentor = this.rondaMentorRepository.save(rondaMentor);
                savedRondaMentors.add(saveRondaMentor);
            }
            createdRonda.setRondaMentors(savedRondaMentors);
        }
        if(Utilities.listHasElements(rondaDTO.getRondaMentees())) {
            List<RondaMentee> rondaMentees = ronda.getRondaMentees();
            List<RondaMentee> savedRondaMentees = new ArrayList<>();
            for (RondaMentee rondaMentee: rondaMentees) {
                rondaMentee.setRonda(createdRonda);
                Tutored mentee = tutoredRepository.findByUuid(rondaMentee.getTutored().getUuid()).get();
                rondaMentee.setTutored(mentee);
                rondaMentee.setCreatedBy(user.getUuid());
                rondaMentee.setCreatedAt(DateUtils.getCurrentDate());
                rondaMentee.setLifeCycleStatus(LifeCycleStatus.ACTIVE);
                RondaMentee savedRondaMentee = this.rondaMenteeRepository.save(rondaMentee);
                savedRondaMentees.add(savedRondaMentee);
            }
            createdRonda.setRondaMentees(savedRondaMentees);
        }
        RondaDTO dto = new RondaDTO(createdRonda);
        return dto;
    }

    public List<Ronda> getAllOfMentor(String mentorUuid) {
        List<Ronda> rondas = this.rondaRepository.getAllOfMentor(mentorUuid);
        for (Ronda ronda: rondas) {
           List<RondaMentee> rondaMentees =  rondaMenteeRepository.findByRonda(ronda.getId(), LifeCycleStatus.ACTIVE);
           ronda.setRondaMentees(rondaMentees);

           List<RondaMentor> rondaMentors = rondaMentorRepository.findByRonda(ronda.getId(), LifeCycleStatus.ACTIVE);
           ronda.setRondaMentors(rondaMentors);
        }

        return rondas;
    }

    public boolean doesUserHaveRondas(User user) {
        List<Ronda> rondas = this.rondaRepository.getByUserUuid(user.getUuid());
        return !rondas.isEmpty();
    }

    public boolean doesHealthFacilityHaveRondas(HealthFacility healthFacility) {
        List<Ronda> rondas = this.rondaRepository.getByHealthFacilityId(healthFacility.getId());
        return !rondas.isEmpty();
    }
}
