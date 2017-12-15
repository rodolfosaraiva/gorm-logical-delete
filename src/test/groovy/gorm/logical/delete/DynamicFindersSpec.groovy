package gorm.logical.delete

import grails.gorm.annotation.Entity
import grails.gorm.transactions.Rollback
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * This test suite focuses on the behavior of dynamic finders in collaboration with the PreQuery Listener
 */
class DynamicFindersSpec extends Specification implements DomainUnitTest<PersonB> {

    Closure doWithSpring() { { ->
            queryListener PreQueryListener
        }
    }

    /******************* test FindAll ***********************************/

    @Rollback
    void 'test dynamic findAll hide logical deleted items'() {
        given:
        createUsers()

        // findAll() Call
        when:
        assert PersonB.count() == 3
        PersonB.findByUserName("Ben").delete()
        PersonB.findByUserName("Nirav").delete()
        List<PersonB> results = PersonB.findAll()

        then: "we should only get those not logically deleted"
        results.size() == 1
        results[0].userName == 'Jeff'

        // list() calll
        when:
        results.clear()
        results = PersonB.list()

        then:
        results.size() == 1
        results[0].userName == 'Jeff'
    }

    /***************** test findBy ***************************/

    @Rollback
    void 'test dynamic findByUserName hide logical deleted items'() {
        given:
        createUsers()

        // findByUserName() Call
        when:
        assert PersonB.count() == 3
        PersonB.findByUserName("Ben").delete()
        PersonB.findByUserName("Nirav").delete()
        PersonB result1 = PersonB.findByUserName("Ben")
        PersonB result2 = PersonB.findByUserName("Nirav")

        then:  "we shouldn't get any bc it was deleted"
        !result1
        !result2
    }

    /***************** test findByDeleted ***************************/

    @Rollback
    void 'test dynamic findByDeleted hide logical deleted items'() {
        given:
        createUsers()

        // findByDeleted() Call
        when:
        assert PersonB.count() == 3
        PersonB.findByUserName("Ben").delete()
        PersonB.findByUserName("Nirav").delete()
        List<PersonB> results = PersonB.findAllByDeleted(true)

        then: "we should not get any because these are logically deleted"
        results.size() == 0
        results.clear()

        when:
        results = PersonB.findAllByDeleted(false)

        then: "we should find the entity because it is not logically deleted"
        results.size() == 1
        results[0].userName == 'Jeff'
    }

    /***************** test get() ***************************/

    @Rollback
    void 'test dynamic get() finds logical deleted items'() {
        given:
        createUsers()

        when: "when 'get()' is used, we can access logically deleted entities"
        assert PersonB.count() == 3
        PersonB.findByUserName("Ben").delete()
        PersonB.findByUserName("Nirav").delete()
        def ben = PersonB.get(1)
        def nirav = PersonB.get(2)

        then:
        nirav.userName == "Nirav" && nirav.deleted
        ben.userName == "Ben" && ben.deleted
    }

    /********************* setup *****************************/

    private List<PersonB> createUsers() {
        def ben = new PersonB(userName: "Ben").save(flush: true)
        def nirav = new PersonB(userName: "Nirav").save(flush: true)
        def jeff = new PersonB(userName: "Jeff").save(flush: true)
        [ben, nirav, jeff]
    }
}

/**************** GORM Entity *****************************/

@Entity
class PersonB implements LogicalDelete {
    String userName

    String toString() {
        "$userName"
    }
}