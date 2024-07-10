package mz.org.fgh.mentoring.service.session;

import jakarta.inject.Singleton;
import mz.org.fgh.mentoring.entity.mentorship.Mentorship;
import mz.org.fgh.mentoring.entity.ronda.Ronda;
import mz.org.fgh.mentoring.entity.session.Session;
import mz.org.fgh.mentoring.repository.answer.AnswerRepository;
import mz.org.fgh.mentoring.repository.ronda.RondaRepository;
import mz.org.fgh.mentoring.repository.session.SessionRepository;
import mz.org.fgh.mentoring.util.LifeCycleStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class SessionService {

    SessionRepository sessionRepository;

    RondaRepository rondaRepository;

    AnswerRepository answerRepository;

    public SessionService(SessionRepository sessionRepository, RondaRepository rondaRepository, AnswerRepository answerRepository) {
        this.sessionRepository = sessionRepository;
        this.rondaRepository = rondaRepository;
        this.answerRepository = answerRepository;
    }

    public List<Session> getAllRondas(List<String> rondasUuids) {
        List<Ronda> rondas = rondaRepository.findRondasByUuids(rondasUuids);
        List<Session> sessions = new ArrayList<>();
        for (Ronda ronda: rondas) {
            Optional<Session> optSession = sessionRepository.findByRonda(ronda.getId());
            if(optSession.isPresent()) {
                Session session = optSession.get();
                List<Mentorship> mentorships = session.getMentorships();
                for (Mentorship mentorship : mentorships) {
                    mentorship.setAnswers(answerRepository.fetchByMentorshipUuid(mentorship.getUuid(), LifeCycleStatus.ACTIVE));
                }
                sessions.add(session);
            }
        }
        return sessions;
    }

    public Session findByUuid(String uuid) {
        return sessionRepository.findByUuid(uuid).orElseThrow(() -> new IllegalArgumentException("Session not found"));
    }
}
