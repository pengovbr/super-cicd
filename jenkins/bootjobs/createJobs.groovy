import hudson.plugins.git.*;
import com.cloudbees.hudson.plugins.folder.*;


def createFolder(pai, name){


    def folder = pai.getItem(name)
    if (folder == null) {
      // Create the folder if it doesn't exist or if no existing job has the same name
      folder = pai.createProject(Folder.class, name)
    }
}

def createJob(pai, name, caminho, prefix_path){

    exists = pai.getItem(name)
    if (exists == null){
		
		repo_git = 'https://github.com/pengovbr/super-cicd.git'
		repo_caminho_job = "indef"
		repo_branch = "main"
		repo_credencial_id = ""
		
		if(name.endsWith(".jobconfig")){

			Properties properties = new Properties()
			conteudo = new File("${WORKSPACE}/" + prefix_path + caminho).text
			InputStream is = new ByteArrayInputStream(conteudo.getBytes());
			properties.load(is)
			
			repo_git = properties."GIT"
			repo_caminho_job = properties."JOB"
            repo_branch = properties."BRANCH" ?: "main"
			repo_credencial_id = properties."CREDENTIAL"

		}else if (name.endsWith(".groovy")){
			repo_caminho_job = prefix_path + caminho
		}
		
	    def scm = new GitSCM([new UserRemoteConfig(repo_git, null, null, repo_credencial_id)], [new BranchSpec(repo_branch)], false, null, null, null, null)

	    def flowDefinition = new org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition(scm, repo_caminho_job)
		flowDefinition.setLightweight(true)
		
        def job = new org.jenkinsci.plugins.workflow.job.WorkflowJob(pai, name)
        job.definition = flowDefinition
    }
}

def createFromStringPath(completePath, prefix_path){
	
    def j = Jenkins.instance
    def f = j
	
    dirs = completePath.split("/")
    
    
    println dirs.length + 'sdf' + dirs[0]
    
    for(i=0; i<dirs.length; i++){
        
        if (i==dirs.length-1){
            createJob(f, dirs[i], completePath, prefix_path)
        }else{
            println "criar folder" + dirs[i]
            //f = f.getItem(dirs[i])
            createFolder(f, dirs[i])
            f = f.getItem(dirs[i])
        }
        
        
    }
    
        
}

def getDirAndCreate(diretorio){

    def  FILES_LIST = sh (script: """find ${diretorio} -type f -printf '%P\n'""", returnStdout: true).trim()
    for(String ele : FILES_LIST.split("\\r?\\n")){ 
        
		
		println ">>>${ele}<<<"
		//def content = readFile "${diretorio}/${ele}"
        //println content
        
        createFromStringPath(ele, diretorio)
        
    }

}


node(){

git url: 'https://github.com/pengovbr/super-cicd.git', branch: "main"

    sh """ 
    pwd
    ls -l
	echo ${WORKSPACE}
     """

getDirAndCreate('jenkins/jobs/')

println "Saiu"

def parent = Jenkins.instance
parent.reload()
}