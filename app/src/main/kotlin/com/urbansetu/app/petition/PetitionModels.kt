package com.urbansetu.app.petition

data class Petition(
  val title: String,
  val ward: String,
  val summary: String,
  val targetAuthority: String,
  val contactEmail: String
)
