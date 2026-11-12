#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script for Linux/macOS
# ----------------------------------------------------------------------------

set -e

MAVEN_VERSION="3.9.9"
MAVEN_USER_HOME="${HOME}/.m2"
MAVEN_DIR="${MAVEN_USER_HOME}/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_HOME="${MAVEN_DIR}/apache-maven-${MAVEN_VERSION}"

if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
    echo "Baixando Apache Maven ${MAVEN_VERSION}..."
    ZIP_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip"
    mkdir -p "${MAVEN_DIR}"
    ZIP_FILE="${MAVEN_DIR}/maven.zip"

    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "${ZIP_URL}" -o "${ZIP_FILE}"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "${ZIP_URL}" -O "${ZIP_FILE}"
    else
        echo "Erro: curl ou wget não encontrado para baixar o Maven." >&2
        exit 1
    fi

    if command -v unzip >/dev/null 2>&1; then
        unzip -q -o "${ZIP_FILE}" -d "${MAVEN_DIR}"
    elif command -v jar >/dev/null 2>&1; then
        (cd "${MAVEN_DIR}" && jar -xf "${ZIP_FILE}")
    elif command -v python3 >/dev/null 2>&1; then
        python3 -c "import zipfile; zipfile.ZipFile('${ZIP_FILE}').extractall('${MAVEN_DIR}')"
    else
        echo "Erro: unzip não encontrado para extrair o Maven." >&2
        exit 1
    fi

    rm -f "${ZIP_FILE}"
    chmod +x "${MAVEN_HOME}/bin/mvn" 2>/dev/null || true
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
