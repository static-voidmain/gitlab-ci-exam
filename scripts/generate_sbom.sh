#!/usr/bin/env bash
set -euo pipefail

cat > sbom-ant.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<bom xmlns="http://cyclonedx.org/schema/bom/1.4" version="1">
  <components>
    <component type="application">
      <name>ant-app</name>
      <version>1.0.0</version>
      <purl>pkg:maven/com.example/ant-app@1.0.0</purl>
    </component>
  </components>
</bom>
EOF

echo "Generated sbom-ant.xml"
