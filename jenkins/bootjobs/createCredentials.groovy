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

// separador de campo: @@#@@
// separador de linha : &&& 
// note q o backslash devera estar duplicado (cada 2 vira 1 na sua senha/texto)
//ver Readme
// abaixo um ex. vai criar uma primeira credencial do tipo usernamepass com o nome zzjenkins-agents, descricao = desc
// username: jenkins - password: nova@a#a$a%a&a|a\a(a?a
// note q o backslash devera estar duplicado
// mais abaixo criar um texto secreto com nome: zcredModuloPenCert descricao=desc texto=jlkhjklhjkl hjklhjklhjkl
// mais abaixo cria um arquivo secreto com nome: zsecfile nome_arquvo: arquivo.txt e texto do aquivo: 
//---begin
//asdfhjkl
//end----
// esse campo secrets nao pode ultrapassar 65000 caracteres. caso ultrapasse esse valor rode em mas de uma rodada

secrets = '''
usernamepass@@#@@zzjenkins-agents@@#@@desc@@#@@jenkins@@#@@nova@a#a$a%a&a|a\\a(a?a&&&
usernamepass@@#@@zzjenkinsagent-ssh@@#@@desc@@#@@jenkins@@#@@jenkinsuser&&&
usernamepass@@#@@zzmpgitlab@@#@@desc@@#@@jenkins@@#@@nTsYOYadhyUVXPuzxes5iBi6eQZmSpHOSeDfekTj&&&
secrettext@@#@@zcredModuloPenCert@@#@@desc@@#@@jlkhjklhjkl hjklhjklhjkl&&&
secrettext@@#@@zcredModuloPenCert2@@#@@desc2@@#@@2jlkhjklhjkl hjklhjklhjkl&&&
sshprivkey@@#@@zgitcredmoduloresposta@@#@@desc@@#@@username@@#@@---begin
asdfhjkl
end----@@#@@passp&&&
sshprivkey@@#@@z2gitcredmoduloresposta@@#@@desc@@#@@username@@#@@---begin
asdfhjkl
end----@@#@@passp&&&
secretfile@@#@@zsecfile@@#@@desc@@#@@arquivo.txt@@#@@---begin
asdfhjkl
end----&&&
secretfile@@#@@zsecfile2@@#@@desc@@#@@arquivo2.txt@@#@@---begin
dddd2222
end----&&&
'''

secretsarr = secrets.trim().split("&&&")


secretsarr.each{ value ->
    //println value
    s = value.trim().split("@@#@@")
    
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


