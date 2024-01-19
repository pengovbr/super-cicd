import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import com.cloudbees.plugins.credentials.CredentialsScope
import hudson.util.Secret
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl;



def createCred(tipo, id, desc, username, password, secrettext, filenamedesired, filetextorprivkey, passphrase){

    instance = Jenkins.instance
    domain = Domain.global()
    store = instance.getExtensionList(
      "com.cloudbees.plugins.credentials.SystemCredentialsProvider")[0].getStore()
    
    switch(tipo) {
        case "usernamepass":

            c = new UsernamePasswordCredentialsImpl(
                CredentialsScope.GLOBAL,
                id,
                desc,
                username,
                password
            )
            break;
            
        case "secrettext": 
           
            c = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
                id,
                desc,
                Secret.fromString(secrettext)
            )
            break;
        
        case "sshprivkey":
   
            privateKey = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(filetextorprivkey)
            
        	c = new BasicSSHUserPrivateKey(
        	    CredentialsScope.GLOBAL,
        	    id,
        	    username,
        	    privateKey,
        	    passphrase,
        	    desc
        	    )
            break;
            
        
        case "secretfile": 
       
            secretBytes = SecretBytes.fromBytes(filetextorprivkey.getBytes())
        	c = new FileCredentialsImpl(CredentialsScope.GLOBAL, id, desc, filenamedesired, secretBytes)
            break;
            

      } 
      

    store.addCredentials(domain, c)

}


secrets = """
usernamepass|zzjenkins-agents|desc|jenkins|jenkinsuser&&&
usernamepass|zzjenkinsagent-ssh|desc|jenkins|jenkinsuser&&&
usernamepass|zzmpgitlab|desc|jenkins|nTsYOYadhyUVXPuzxes5iBi6eQZmSpHOSeDfekTj&&&
secrettext|zcredModuloPenCert|desc|jlkhjklhjkl hjklhjklhjkl&&&
secrettext|zcredModuloPenCert2|desc2|2jlkhjklhjkl hjklhjklhjkl&&&
sshprivkey|zgitcredmoduloresposta|desc|username|---begin
asdfhjkl
end----|passp&&&
sshprivkey|z2gitcredmoduloresposta|desc|username|---begin
asdfhjkl
end----|passp&&&
secretfile|zsecfile|desc|arquivo.txt|---begin
asdfhjkl
end----&&&
secretfile|zsecfile2|desc|arquivo2.txt|---begin
dddd2222
end----&&&
"""

//secrets = secrets.replace("&&&\\n", "&&&")
//println secrets
secretsarr = secrets.trim().split("&&&")

/*
for (i=0;i<1;i++){

    s = secretsarr[i].trim().split("\\|")
    switch(s[0]){
        case "usernamepass":
            println s[1]
            createCred(s[0], s[1], s[2], s[3], s[4], "", "", "", "")
            break;
    }

}
*/
secretsarr.each{ value ->
    //println value
    s = value.trim().split("\\|")
    
    println s[0]
    println s[1]
    println ""
    
    switch(s[0]){
        case "usernamepass":
            createCred(s[0], s[1], s[2], s[3], s[4], "", "", "", "")
            break;
            
        case "secrettext":
            createCred(s[0], s[1], s[2], "", "", s[3], "", "", "")
            break;
            
        case "sshprivkey":
            createCred(s[0], s[1], s[2], s[3], "", "", "", s[4], s[5])
            break;
            
        case "secretfile":
            createCred(s[0], s[1], s[2], "", "", "", s[3], s[4], "")
            break;
    }
    
}


