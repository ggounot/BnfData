# Auteurs et œuvres

Ce dépôt contient le code source de l’application Android *Auteurs et œuvres*.

This repository contains the source code of the Android application *Auteurs et
œuvres*.

<a href="https://play.google.com/store/apps/details?id=eu.gounot.bnfdata">
  <img alt="Get it on Google Play"
       src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

## Base de données

L'application comporte une base de données SQLite locale utilisée pour la
recherche des formes. Celle-ci est trop volumineuse pour être incluse dans le
dépôt Git. Vous pouvez trouver les fichiers SQLite compressés correspondants
aux différentes versions de l'application à l’URL suivante :
http://bnfdata.gounot.eu/database/

Pour construire l’application, vous devez télécharger la version correspondante
du fichier SQLite compressé, changer l’extension **.tar.gz** en **.jpg** et la
copier dans un répertoire *assets* à la racine de l’arborescence du projet.

### Pourquoi le fichier SQlite doit avoir l’extension .jpg ?

L’outil d’empaquetage AAPT compresse et décompresse de manière transparente les
fichiers du répertoire *assets*, sauf ceux qui comportent une extension connue
de fichier déjà compressé, comme *.jpg*. Les versions d’Android antérieure à la
version 2.3 ne supportent pas la décompression transparente des fichiers dont
la taille décompressée est supérieure à 1 Mo. Pour cette raison, nous gérons
nous même la compression et la décompression du fichier et employons
l’extension *.jpg* pour empêcher AAPT de réaliser ses propres compression et
décompression qui provoqueraient l’erreur sous Android 2.2.

