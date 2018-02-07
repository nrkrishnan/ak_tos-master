import com.navis.argo.ContextHelper
import com.navis.argo.business.api.GroovyApi
import com.navis.argo.business.atoms.IntegrationTypeEnum
import com.navis.argo.business.integration.jms.IArgoJmsManager
import com.navis.argo.business.integration.jms.JmsIntegrationFailure
import com.navis.argo.business.model.Facility
import com.navis.carina.integrationservice.business.IntegrationService
import com.navis.framework.IntegrationServiceField
import com.navis.framework.business.Roastery
import com.navis.framework.business.atoms.EsbConfigurationTypeEnum
import com.navis.framework.business.atoms.IntegrationServiceDirectionEnum
import com.navis.framework.business.atoms.IntegrationServiceTypeEnum
import com.navis.framework.business.atoms.JmsProviderEnum
import com.navis.framework.esb.client.IESBClient
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.persistence.hibernate.CarinaPersistenceCallback
import com.navis.framework.persistence.hibernate.PersistenceTemplate
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.UserContext
import com.navis.framework.portal.context.UserContextUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.scope.ScopeCoordinates
import org.apache.commons.lang.StringUtils;


/**
 * @author Keerthi Ramachandran
 * @since 8/24/2017
 * <p>SendXmlAtComplexLevel is helper class to publish XML messages to ANK outbound queue, where for some messages the
 * facility is null as the operation happens at the Complex Level</p>
 */
class SendXmlAtComplexLevel extends GroovyApi {

    public static final String DEFAULT_FACILITY = "ANK";

    /**
     * sendXmlFromComplexLevel
     * @param inXmlMessage
     * <p>New Default Method to send all the XML Messages, here we'll use for the only call</p>
     */
    void sendXmlFromComplexLevel(String inXmlMessage) {
        Facility fcy = ContextHelper.getThreadFacility();
        if (fcy != null) {
            this.sendXml(inXmlMessage);
        } else {
            sendXmlToDefaultQueue(inXmlMessage);
        }
    }

    void sendXmlToDefaultQueue(String inXmlMessage) {
        Facility facility = Facility.findFacility(DEFAULT_FACILITY);
        String jmsURI = this.getOutboundUriFromIntegrationService(facility.getFcyGkey());
        if (StringUtils.isNotEmpty(jmsURI)) {
            IESBClient eSBClient = (IESBClient) Roastery.getBean("esbClient");

            try {
                eSBClient.dispatchNotification(EsbConfigurationTypeEnum.SNX, jmsURI, inXmlMessage, (Map) null);
            } catch (Exception inException) {
                String message = "Unexpected JMS connection problem";
                LOGGER.error(message, inException);
                throw new JmsIntegrationFailure(jmsURI, message, inException, inXmlMessage, IntegrationTypeEnum.JMS_OUTBOUND_TEXT);
            }
        } else {
            String message;
            if (jmsURI == null) {
                message = facility.getFcyPathName() + " has no default outbound jms integration service. (Complex Level Customization)";
            } else {
                message = facility.getFcyPathName() + " has no outbound jms integration service.  (Complex Level Customization)";
            }

            LOGGER.error(message);
            throw new JmsIntegrationFailure(jmsURI, message, (Throwable) null, inXmlMessage, IntegrationTypeEnum.JMS_OUTBOUND_TEXT);
        }
    }

    private String getOutboundUriFromIntegrationService(final Serializable inFcyGkey) {
        final ArrayList col = new ArrayList();
        UserContext userContext = UserContextUtils.getSystemUserContext();
        PersistenceTemplate pt = new PersistenceTemplate(userContext);
        pt.invoke(new CarinaPersistenceCallback() {
            protected void doInTransaction() {
                DomainQuery dq = QueryUtils.createDomainQuery("IntegrationService").
                        addDqField(IntegrationServiceField.INTSERV_NAME).
                        addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_DIRECTION, IntegrationServiceDirectionEnum.OUTBOUND)).
                        addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_SCOPE_GKEY, inFcyGkey)).
                        addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_SCOPE_LEVEL, ScopeCoordinates.SCOPE_LEVEL_3)).
                        addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_TYPE, IntegrationServiceTypeEnum.JMS)).
                        addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_ACTIVE, Boolean.TRUE)).
                        addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_DEFAULT, Boolean.TRUE));

                List values = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                col.addAll(values);
            }
        });
        if (col.size() > 0) {
            IntegrationService service = (IntegrationService) col.iterator().next();
            return "jms://" + service.getIntservName() + "?connector=" + JmsProviderEnum.ACTIVE_MQ.getKey() + service.getIntservName();
        } else {
            return null;
        }
    }
}
