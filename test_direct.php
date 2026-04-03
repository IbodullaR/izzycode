<?php
function helloWorld() { return "Hello, World!"; }

ob_start();
$input = trim(fgets(STDIN));
if (empty($input)) {
    $result = helloWorld();
} else {
    $result = helloWorld();
}
$captured = ob_get_clean();

if (!empty($captured)) {
    echo trim($captured) . "\n";
} elseif ($result !== null) {
    echo $result . "\n";
}
?>
