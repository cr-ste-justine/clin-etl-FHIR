package org.crsj.clin.etl

import org.apache.spark.sql
import org.apache.spark.sql.{DataFrame, SparkSession}

object ClinicalImpression {

  def load(base: String, practitionerWithRolesAndOrg: sql.DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._
    val clinicalImpression = DataFrameUtils.load(s"$base/ci.ndjson", $"id", $"subject",
      $"status", $"effective", $"extension.valueAge.value" (0) as "runtimePatientAge", $"assessor.id" as "assessor_role_id",
      $"investigation.item" (0) as "iiu")

    val clinicalImpressionWithAssessor = clinicalImpression
      .select($"id", $"subject", $"status", $"effective" as "ci_consultation_date", $"runtimePatientAge", $"assessor_role_id", $"iiu")
      .join(practitionerWithRolesAndOrg
      .select($"role_id", $"name" as "assessor_name", $"org_name" as "assessor_org_name", $"org_id" as "assessor_org_id", $"id" as "assessor_id"),
        $"assessor_role_id" === $"role_id")
    clinicalImpressionWithAssessor
  }
}
