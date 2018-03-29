package restface;

import authdb.Role;
import authdb.User;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Random;
import javax.persistence.NoResultException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import JWT.JWTBuilder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;

@Path("2fa")
@Stateless
public class TwoFactorAuth {

    @Context
    private UriInfo context;
    
    @PersistenceContext
    private EntityManager em;

    public TwoFactorAuth() {
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("identity")
    public Response Identity(String identity) {
        try{
            //Select role by name from realm  
            Role role =null;
            try{
                role = (Role)em.createNamedQuery("findByRoleName")
                .setParameter("roleName", "debit-card-lead")
                .getSingleResult();
            }
            catch(NoResultException e){
                 //Role not found
                 throw new WebApplicationException("3.01.001",Response.Status.EXPECTATION_FAILED);
            }            
            //Select user by login from realm       
            User user;
            try{
                user = (User)em.createNamedQuery("findByLogin")
                    .setParameter("login", identity)
                    .getSingleResult();
                //Check 5 minutes timstamp
                Date controlTime= new Date(user.getUpdateTime().getTime()+60000);
                Date now = new Date();
                if (now.after(controlTime)){
                    //Update timestamp 
                    user.setUpdateTime(now);
                }
                else{
                   //throw new WebApplicationException("3.01.000",Response.Status.EXPECTATION_FAILED); 
                } 
            }
            //Add new user to realm and assigning it the debit-card-lead role.
            catch(NoResultException e){ 
               user = new User(identity); 
               user.addRole(role);               
            }
            
            //User is created. Timestamp is checked. Generating access code. 
            int  n = new Random().nextInt(8999)+1000;
            //Set new password
            user.setPassword(Integer.toString(n));
            //We are ready to commit to realm.
            em.persist(user);
            em.flush();
           
            //Realm have been commited. We are redy to send SMS with access code. 
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element IMAP = doc.createElement("IMAP");
            Element Message = doc.createElement("Message"); Attr id = doc.createAttribute("id");
            Element SMSAlert = doc.createElement("SMSAlert");
            Element version = doc.createElement("version");
            Element msisdn = doc.createElement("msisdn");
            Element text = doc.createElement("text"); 
            doc.appendChild(IMAP);
                IMAP.appendChild(Message);
                    Message.appendChild(SMSAlert); Message.setAttributeNode(id); id.setValue(java.util.UUID.randomUUID().toString());
                        SMSAlert.appendChild(version); version.appendChild(doc.createTextNode("1.2.0"));
                        SMSAlert.appendChild(msisdn); msisdn.appendChild(doc.createTextNode(identity));
                        SMSAlert.appendChild(text); text.appendChild(doc.createTextNode("Kod dliya podtverzhdeniya zayavki "+user.getPassword()+". Raiffeisenbank"));  
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
 
            //Send SMS with access code.
            String payload = sw.toString();   
            StringEntity entity = new StringEntity(payload, ContentType.TEXT_XML);
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost("http://10.242.133.55:8081");
            request.setEntity(entity);
            //HttpResponse response = httpClient.execute(request);
            //System.out.println(response.getStatusLine().getStatusCode());
            
            //Done
            throw new WebApplicationException(payload,Response.Status.OK);
        }
        catch(WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build(); 
        } 
        catch(RuntimeException | ParserConfigurationException | TransformerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();  
        } 
        //catch (IOException e) {
        //    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        //}
    }
 
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("auth")
    public Response Auth(String login,@Context SecurityContext sc) {
        try{        
            JWTBuilder builder = new JWTBuilder();
            String header = "{\"alg\":\"none\",\"typ\":\"JWT\"}";
            Date expTime= new Date(new Date().getTime()+60000);   
           
            String payload = "{\"iss\":\"www.raiffeisen.ru\",\"sub\":\"89165134274\",\"exp\":2051222399,\"aud\":\"elytron-test\",\"groups\":\"debit-card-lead\"}";
           
         //   String payload = "{\"sub\":\"elytron@wildfly.org\",\"iss\":\"issuer.wildfly.org\",\"aud\":\"elytron-test\"}";
            String base64Header = builder.encodeBase64(header.getBytes());
            String base64Payload = builder.encodeBase64(payload.getBytes());
            String base64signature = builder.signHmacSHA256Base64(base64Header, base64Payload, "secret");
            
            String decode64header = builder.decodeBase64(base64Header.getBytes());
            String decode64Payload = builder.decodeBase64(base64Payload.getBytes());
            throw new WebApplicationException(base64Header+"."+base64Payload+".",Response.Status.OK);
            //throw new WebApplicationException(header+"."+payload+".",Response.Status.OK);
           // throw new WebApplicationException(qq,Response.Status.OK);
            
        }
        catch(WebApplicationException e) 
        {
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build(); 
        } 
        catch (InvalidKeyException | NoSuchAlgorithmException e) 
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
   // @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("oper")
    //@RolesAllows("ADMIN")
    public Response getOper(String p, @Context SecurityContext sc, @Context HttpServletRequest req ) {
      
       String qq = req.getRemoteAddr();
//if (sc.isUserInRole("not-identity-principal))  
      if (sc.isUserInRole("not-identity-principal"))
      {
         Principal principal =  sc.getUserPrincipal();
              return Response.status(Response.Status.UNAUTHORIZED).entity(sc.getUserPrincipal().toString()+", "+qq).build();  
 
        // return Response.status(Response.Status.OK).entity(principal.getName()).build(); 
      }
      return Response.status(Response.Status.UNAUTHORIZED).entity(sc.getUserPrincipal().toString()+", "+qq).build();  
    }   

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public void putText(String content) {
    }
}
