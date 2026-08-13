param(
    # Usa -CompilarSolamente para verificar el código sin abrir JavaFX.
    [switch]$CompilarSolamente
)

# Esta ruta apunta a la raíz del proyecto sin depender de dónde se ejecute el script.
$raizProyecto = Split-Path -Parent $PSScriptRoot

# Maven guarda las dependencias descargadas dentro de .m2 del usuario actual.
$repositorioMaven = Join-Path $env:USERPROFILE '.m2\repository'

# Se enumeran exactamente las versiones declaradas en pom.xml.
$dependencias = @(
    (Join-Path $repositorioMaven 'org\openjfx\javafx-base\13\javafx-base-13-win.jar'),
    (Join-Path $repositorioMaven 'org\openjfx\javafx-graphics\13\javafx-graphics-13-win.jar'),
    (Join-Path $repositorioMaven 'org\openjfx\javafx-controls\13\javafx-controls-13-win.jar'),
    (Join-Path $repositorioMaven 'org\openjfx\javafx-fxml\13\javafx-fxml-13-win.jar'),
    (Join-Path $repositorioMaven 'org\postgresql\postgresql\42.7.13\postgresql-42.7.13.jar')
)

# Se detiene con un mensaje claro cuando alguna dependencia todavía no existe.
$faltantes = $dependencias | Where-Object { -not (Test-Path -LiteralPath $_) }
if ($faltantes) {
    Write-Error ('Faltan dependencias locales. Ejecuta primero Maven desde tu IDE: ' + ($faltantes -join ', '))
    exit 1
}

# Java recibe las dependencias como una sola cadena separada por punto y coma.
$rutaModulos = $dependencias -join ';'

# target/classes es la salida estándar de compilación del proyecto.
$salidaClases = Join-Path $raizProyecto 'target\classes'
New-Item -ItemType Directory -Path $salidaClases -Force | Out-Null

# Se recopilan todos los .java, incluido module-info.java.
$fuentes = (Get-ChildItem -Path (Join-Path $raizProyecto 'src\main\java') -Recurse -Filter '*.java').FullName

# javac verifica y compila la aplicación modular completa.
javac --module-path $rutaModulos -d $salidaClases $fuentes
if ($LASTEXITCODE -ne 0) {
    Write-Error 'La compilación falló. Revisa los mensajes anteriores.'
    exit $LASTEXITCODE
}

# Los FXML, propiedades y SQL deben quedar junto a las clases compiladas.
Copy-Item -Path (Join-Path $raizProyecto 'src\main\resources\*') `
          -Destination $salidaClases -Recurse -Force

# El parámetro permite usar el mismo script como verificación rápida.
if ($CompilarSolamente) {
    Write-Output 'Compilación terminada correctamente.'
    exit 0
}

# Java inicia el módulo y la clase principal de JavaFX.
Set-Location -LiteralPath $raizProyecto
java --module-path ($rutaModulos + ';' + $salidaClases) `
     --enable-native-access=javafx.graphics `
     --module utng.gtid.jjcm/utng.gtid.jjcm.App
