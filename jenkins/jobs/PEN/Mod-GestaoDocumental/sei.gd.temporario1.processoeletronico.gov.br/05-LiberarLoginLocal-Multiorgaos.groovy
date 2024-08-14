/*
Usuario jenkins precisa ter permissao de sudo
Jenkins minimo em 2.332
Criar secrets de acordo com os parameters informados no job
Precisa de um noh com label temporario1, ou altere abaixo de acordo com seu cluster
*/

pipeline {

    agent {
	    node{
	        label "temporario1"
	    }
	}

    parameters {
		booleanParam(name: 'Instrucoes',
		      defaultValue: false,
		      description: 'Esse job vai permtir o login sem AD no multiorgao para o ambiente http://sei.gd.temporario1.processoeletronico.gov.br/ Usuario e senha mesma coisa')

    }

    stages {

		stage('Lberando Login Multiorgaos'){

			steps {

				script{

					if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        error('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

					DIRECTORY = WORKSPACE
					FOLDERSEIDOCKER = "/home/jenkins/folderseidocker"

					dir("${FOLDERSEIDOCKER}/infra"){
						
						sh """
		                docker exec -t docker-compose-db-1 bash -c "mysql -pP@ssword -e \\"update sip.orgao set sin_autenticar='N';\\""
                        
		                """

					}
					
					
                }

            }

		}


      
    }
}