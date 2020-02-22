param([string]$amServerPort = "0")

$myServers = @("localhost")

foreach ( $hostName in $myServers ) {

	$connProcs = Get-WmiObject -ComputerName $hostName Win32_Process -filter "Name LIKE 'java.exe' AND commandline LIKE '%-Dam.server.port=%'"

	foreach ($proc in $connProcs){
	
		if ( $proc.commandLine -match '(?<=(-Dam.server.port=))\d*(?=( ))' ) {
		
			$port = $matches[0];
			
			if ( $port -eq $amServerPort ) {
			
				$processId = $proc.processId
	
				Write-Warning "Found hung connection: $processId on Host: $hostName, terminating..."
				$returnVal = $proc.terminate()
				Write-Information "Termination status: $returnVal.returnvalue"
				
			} else {
			
				Write-Warning "Hung connection not found with port: $amServerPort, ignoring..."
			
			}
		
		}
	}
}

