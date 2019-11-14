package it.unipi.RoomBooking.Database;

//import java.util.ArrayList;
import java.util.Collection;
//import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import it.unipi.RoomBooking.Data.Interface.Person;
//import it.unipi.RoomBooking.Data.Interface.Room;
import it.unipi.RoomBooking.Data.ORM.*;
import it.unipi.RoomBooking.Exceptions.UserNotExistException;

public class HibernateDriver {
    private static EntityManagerFactory factory;
    private EntityManager entityManager;

    public void start() {
        /* Set Hibernate log level */
        LogManager logManager = LogManager.getLogManager();
        Logger logger = logManager.getLogger("");
        logger.setLevel(Level.SEVERE);

        factory = Persistence.createEntityManagerFactory("roombooking");
    }

    public void exit() {
        factory.close();
    }

    public Person authenticate(String email, boolean isTeacher) throws UserNotExistException {
        try {
            entityManager = factory.createEntityManager();

            if (isTeacher) {
                // Retreive teacher information.
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Teacher> criteriaQuery = criteriaBuilder.createQuery(Teacher.class);
                Root<Teacher> root = criteriaQuery.from(Teacher.class);
                criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("teacherEmail"), email));
                Teacher person = entityManager.createQuery(criteriaQuery).getSingleResult();
                return person;
            } else {
                // Retreive student information.
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Student> criteriaQuery = criteriaBuilder.createQuery(Student.class);
                Root<Student> root = criteriaQuery.from(Student.class);
                criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("studentEmail"), email));
                Student person = entityManager.createQuery(criteriaQuery).getSingleResult();
                return person;
            }

        } catch (NoResultException noex) {
            throw new UserNotExistException("User not found.");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        return null;
    }

    public Collection<Classroom> getAvailableClassrooms() {
        try {
            entityManager = factory.createEntityManager();
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Classroom> criteriaQuery = criteriaBuilder.createQuery(Classroom.class);
            Root<Classroom> root = criteriaQuery.from(Classroom.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("classroomAvailable"), true));
            Collection<Classroom> available = entityManager.createQuery(criteriaQuery).getResultList();
            return available;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        return null;
    }

    public Collection<Laboratory> getAvailableLaboratories(long studentId) {
        try {
            entityManager = factory.createEntityManager();
            // Retreive all the available laboratories
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Laboratory> criteriaQuery = criteriaBuilder.createQuery(Laboratory.class);
            Root<Laboratory> root = criteriaQuery.from(Laboratory.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("laboratoryAvailable"), true));
            Collection<Laboratory> available = entityManager.createQuery(criteriaQuery).getResultList();
            // Delete from the available laboratories the ones already booked by the student
            entityManager.getTransaction().begin();
            Student student = entityManager.find(Student.class, studentId);
            for (Laboratory iteration : student.getLaboratories()) {
                if (available.contains(iteration)) {
                    available.remove(iteration);
                }
            }
            entityManager.getTransaction().commit();
            return available;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        return null;
    }

    public Collection<ClassroomBooking> getBookedClassrooms(long teacherId) {
        try {
            entityManager = factory.createEntityManager();
            // Retreive the booked rooms from the teacher entity.
            entityManager.getTransaction().begin();
            Teacher teacher = entityManager.find(Teacher.class, teacherId);
            entityManager.getTransaction().commit();
            return teacher.getBooked();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        return null;
    }

    public Collection<Laboratory> getBookedLaboratories(long studentId) {
        try {
            entityManager = factory.createEntityManager();
            // Retreive the booked rooms from the student entity.
            entityManager.getTransaction().begin();
            Student student = entityManager.find(Student.class, studentId);
            Collection<Laboratory> booked = student.getLaboratories();
            entityManager.getTransaction().commit();
            return booked;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        return null;
    }

    public void setClassroomBooking(long teacherId, long roomId, String schedule) {
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();

            Teacher teacher = entityManager.find(Teacher.class, teacherId);
            Classroom classroom = entityManager.find(Classroom.class, roomId);
            ClassroomBooking classroombooking = new ClassroomBooking();

            classroombooking.setRoom(classroom);
            classroombooking.setSchedule(schedule);
            classroombooking.setPerson(teacher);
            classroom.setBooking(classroombooking);

            entityManager.persist(classroombooking);
            entityManager.merge(classroom);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public void setLaboratoryBooking(long studentId, long roomId) {
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();

            Laboratory laboratory = entityManager.find(Laboratory.class, roomId);
            Student student = entityManager.find(Student.class, studentId);

            laboratory.setStudent(student);
            student.setLaboratories(laboratory);
            entityManager.merge(laboratory);
            entityManager.merge(student);

            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public void updateAvailability(String roomType, long roomId, boolean flag) {
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();
            if (roomType.equals("cla")) {
                Classroom classroom = entityManager.find(Classroom.class, roomId);
                classroom.setAvailable(flag);
                entityManager.merge(classroom);
            } else {
                Laboratory laboratory = entityManager.find(Laboratory.class, roomId);
                laboratory.setAvailable(flag);
                entityManager.merge(laboratory);
            }
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public boolean getAvailability(String roomType, long roomId) {
        boolean flag = false;
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();
            if (roomType.equals("cla")) {
                ClassroomBooking classroomBooking = entityManager.find(ClassroomBooking.class, roomId);
                Classroom classroom = (Classroom)classroomBooking.getClassroom();
                flag = classroom.getAvailable();
            } else {
                Laboratory laboratory = entityManager.find(Laboratory.class, roomId);
                flag = laboratory.getAvailable();
            }
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        return flag;
    }

    public void deleteClassroomBooking(long bookingId) {
        try {
            entityManager = factory.createEntityManager();
            // Retreiving the room and the reservation to delete and do it.
            entityManager.getTransaction().begin();
            ClassroomBooking classroomBooking = entityManager.find(ClassroomBooking.class, bookingId);
            Classroom classroom = entityManager.find(Classroom.class, classroomBooking.getClassroom().getId());
            classroom.deleteBooking(classroomBooking);

            entityManager.remove(classroomBooking);
            entityManager.merge(classroom);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public void deleteLaboratoryBooking(long studentId, long laboratoryId) {
        try {
            entityManager = factory.createEntityManager();
            // Retreiving the room and the student for delete the reservation.
            entityManager.getTransaction().begin();
            Laboratory laboratory = entityManager.find(Laboratory.class, laboratoryId);
            Student student = entityManager.find(Student.class, studentId);
            laboratory.deleteBooking(student);
            student.deleteBooking(laboratory);

            entityManager.merge(laboratory);
            entityManager.merge(student);

            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public Classroom getClassroom(long bookingId) {
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();

            ClassroomBooking classroomBooking = entityManager.find(ClassroomBooking.class, bookingId);
            Classroom classroom = entityManager.find(Classroom.class, classroomBooking.getClassroom().getId());
            return classroom;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        return null;
    }

    public Laboratory getLaboratory(long laboratoryId) {
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();

            Laboratory laboratory = entityManager.find(Laboratory.class, laboratoryId);
            return laboratory;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        return null;

    }

    /* Admin Voids */
    public void createBuilding(String name, String address) {
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();

            Building building = new Building();

            building.setName(name);
            building.setAddress(address);
            entityManager.merge(building);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public void createStudent(String name, String lastName, String email) {
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();

            Student student = new Student();

            student.setName(name);
            student.setLastname(lastName);
            student.setEmail(email);
            entityManager.merge(student);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public void createTeacher(String name, String lastName, String email) {
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();

            Teacher student = new Teacher();

            student.setName(name);
            student.setLastname(lastName);
            student.setEmail(email);
            entityManager.merge(student);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }



    // if (person instanceof Teacher) {
    // // Retreive all the classroom from the database.
    // CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    // CriteriaQuery<Classroom> criteriaQuery =
    // criteriaBuilder.createQuery(Classroom.class);
    // Root<Classroom> root = criteriaQuery.from(Classroom.class);
    // criteriaQuery.select(root);

    // List<Classroom> classrooms =
    // entityManager.createQuery(criteriaQuery).getResultList();

    // Collection<Classroom> available = new ArrayList<Classroom>();

    // for (Classroom iteration : classrooms) {
    // // Choose only the classrooms that
    // // are available for the entire day or the half
    // // of the day we are looking for.
    // if (iteration.getAvailable()) {
    // if (iteration.getBooking().size() != 0) {
    // if (!iteration.getBooking().iterator().next().getSchedule().equals(schedule))
    // {
    // available.add(iteration);
    // }
    // } else {
    // available.add(iteration);
    // }
    // }
    // }

    // return available;
    // } else { // Retreive all the available laboratories
    // CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    // CriteriaQuery<Laboratory> criteriaQuery =
    // criteriaBuilder.createQuery(Laboratory.class);
    // Root<Laboratory> root = criteriaQuery.from(Laboratory.class);

    // criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("laboratoryAvailable"),
    // true));
    // List<Laboratory> available =
    // entityManager.createQuery(criteriaQuery).getResultList();
    // // Delete from the available laboratories the ones already booked by the
    // student
    // entityManager.getTransaction().begin();
    // Student student = entityManager.find(Student.class, person.getId());

    // for (Laboratory iteration : student.getLaboratories()) {
    // if (available.contains(iteration)) {
    // available.remove(iteration);
    // }
    // }

    // entityManager.getTransaction().commit();
    // return available;
    // }
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // } finally {
    // entityManager.close();
    // }

    // return null;
    // }

    // public Collection<? extends Room> getBooked(Person person) {
    // try {
    // entityManager = factory.createEntityManager();
    // if (person instanceof Teacher) {
    // // Retreive the booked rooms from the teacher entity.
    // entityManager.getTransaction().begin();
    // Teacher teacher = entityManager.find(Teacher.class, person.getId());
    // Collection<ClassroomBooking> booking = teacher.getBooked();
    // Collection<Classroom> booked = new ArrayList<Classroom>();

    // for (ClassroomBooking iteration : booking) {
    // if (!booked.contains((Classroom) iteration.getRoom())) {
    // booked.add((Classroom) iteration.getRoom());
    // }
    // }

    // entityManager.getTransaction().commit();
    // return booked;
    // } else {
    // // Retreive the booked rooms from the student entity.
    // entityManager.getTransaction().begin();
    // Student student = entityManager.find(Student.class, person.getId());
    // Collection<Laboratory> booked = student.getLaboratories();
    // entityManager.getTransaction().commit();

    // return booked;
    // }
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // } finally {
    // entityManager.close();
    // }

    // return null;
    // }

    /*
     * 
     * public void updateBooking(Person person, long oldRoomId, long newRoomId, long
     * bookingId, String newSchedule) { try { entityManager =
     * factory.createEntityManager(); entityManager.getTransaction().begin(); if
     * (person instanceof Teacher) { // Retreiving the old reservation info and the
     * new room to book. Classroom // oldClassroom =
     * entityManager.find(Classroom.class, oldRoomId); Classroom newClassroom =
     * entityManager.find(Classroom.class, newRoomId); ClassroomBooking
     * bookingToRemove = entityManager.find(ClassroomBooking.class, bookingId);
     * ClassroomBooking newClassroomBooking = new ClassroomBooking();
     * oldClassroom.deleteBooking(bookingToRemove); // Check if the old room become
     * available then update it. if (oldClassroom.getBooking().size() < 2 &&
     * !oldClassroom.getAvailable()) { oldClassroom.setAvailable(true); }
     * entityManager.remove(bookingToRemove); entityManager.merge(oldClassroom); //
     * Making the new reservation. newClassroomBooking.setPerson(person);
     * newClassroomBooking.setSchedule(newSchedule);
     * newClassroomBooking.setRoom(newClassroom);
     * entityManager.persist(newClassroomBooking);
     * newClassroom.setBooking(newClassroomBooking); // Check if the new room become
     * unavailable then update it. if (newClassroom.getBooking().size() == 2) {
     * newClassroom.setAvailable(false); } entityManager.merge(newClassroom);
     * entityManager.getTransaction().commit(); } else { // Retreiving the old
     * reservation info and the new room to book. Laboratory oldLaboratory =
     * entityManager.find(Laboratory.class, oldRoomId); Laboratory newLaboratory =
     * entityManager.find(Laboratory.class, newRoomId); Student student =
     * entityManager.find(Student.class, person.getId());
     * oldLaboratory.deleteBooking(student); student.deleteBooking(oldLaboratory);
     * newLaboratory.setStudent(student); student.setLaboratories(newLaboratory); //
     * Check if the old room become available and then update it. if
     * (oldLaboratory.getBookingNumber() < oldLaboratory.getCapacity()) {
     * oldLaboratory.setAvailable(true); } // Check if the new room become
     * unavailable and then update it. if (newLaboratory.getBookingNumber() ==
     * newLaboratory.getCapacity()) { newLaboratory.setAvailable(false); }
     * entityManager.merge(oldLaboratory); entityManager.merge(student);
     * entityManager.merge(newLaboratory); entityManager.getTransaction().commit();
     * } } catch (Exception ex) { ex.printStackTrace(); } finally {
     * entityManager.close(); } }
     */

}