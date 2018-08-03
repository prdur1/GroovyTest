import groovy.json.JsonSlurper
import groovy.util.XmlSlurper
import jenkins.*
import jenkins.model.*
import hudson.model.*

def deployArtifact(ArtifactId,ArtifactVersion,deploymenttarget){		   
		def ServerName = []
		def SiteId = []
		def CC = []
		def EnvType = []
		def jsonSlurper = new JsonSlurper()  
		def deploymentTargetList = jsonSlurper.parseText(DeploymentTarget)   			
		for (i in   0..deploymentTargetList.size()-1) {
			ServerName << deploymentTargetList[i].Server
			SiteId << deploymentTargetList[i].Site
			CC << deploymentTargetList[i].CC
			EnvType << deploymentTargetList[i].Env
		}
		for (i=0; i < ServerName.size(); i++){
			ExecuteSSH (Server, "cd /u/gls/NextGenAppInstall/bin;/u/gls/NextGenAppInstall/bin/AppInstall.rb -c ${CC} -s ${Site} -p  ${ArtifactId} -v ${ArtifactVersion}  -e ${Env} -o stage")
			ExecuteSSH (Server, "cd /u/gls/NextGenAppInstall/bin;/u/gls/NextGenAppInstall/bin/AppInstall.rb -c ${CC} -s ${Site} -p  ${ArtifactId} -v ${ArtifactVersion}  -e ${Env} -o uninstall")
			ExecuteSSH (Server, "cd /u/gls/NextGenAppInstall/bin;/u/gls/NextGenAppInstall/bin/AppInstall.rb -c ${CC} -s ${Site} -p  ${ArtifactId} -v ${ArtifactVersion}  -e ${Env} -o install")
		
		}
}

def ExecuteSSH (Server, cmd){
	def retryCount=2
	while(retryCount>0){
		try{
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'deploymentCredentials', passwordVariable: 'password', usernameVariable: 'username']]) {
			def userName='$username'
			def password='$password'
			sh "ant -DServer=${Server} -Dusername=${userName} -Dpassword=${password} -Dcmd='${cmd}'"
			}
			break
		}catch(e){
			if(retryCount>1){
				retryCount--
			}else{
				throw e
			}
		}	
	}
}