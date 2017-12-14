package gorm.logical.delete

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.event.PreQueryEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Scope

@Scope('singleton')
@Slf4j
@CompileStatic
class PreQueryListener implements ApplicationListener<PreQueryEvent> {

    @Override
    void onApplicationEvent(PreQueryEvent event) {
        try {
            Query query = event.query
            PersistentEntity entity = query.entity

            /*
                if(entity instanceof LogicalDelete)
                I think this would be a better if statement but instanceof does not seem to apply to traits? but
                instanceof only returns User
             */
            if (entity.persistentPropertyNames.contains('deleted')) {
                log.debug 'This entity implements logical delete'

                // Pseudo Code: dont understand how to get this value here as its does not seem to be available, only
                // property name?
                // if allowQueryOfDeletedItems is not true then apply additional filter value
                // allowQueryOfDeletedItems is a property on LogicalDelete that can be overridden by domain classes to
                // specify
                // if we want to allow searching of all deleted properties

                // Need to get value, if value is true
                /*
                if (!entity.persistentPropertyNames.contains('allowQueryOfDeletedItems')) {
                    query.eq('deleted', false)
                }
                */
                query.eq('deleted', false)
            }
        } catch (Exception e) {
            log.error(e.message)
        }
    }
}