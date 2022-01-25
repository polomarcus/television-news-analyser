SELECT date_trunc('month', "public"."news"."date") AS "date",
CASE
    WHEN ((lower("public"."news"."description"))  LIKE '%covid%' OR (lower("public"."news"."title"))  LIKE '%covid%') THEN 'covid'
    WHEN ((lower("public"."news"."description"))  LIKE '%climatique%' OR (lower("public"."news"."title"))  LIKE '%climatique%')  THEN 'rechauffement climatique'
    WHEN ((lower("public"."news"."description"))  LIKE '%giec%' OR (lower("public"."news"."title"))  LIKE '%giec%')  THEN 'GIEC'
    WHEN ((lower("public"."news"."description"))  LIKE '%pib%' OR (lower("public"."news"."title"))  LIKE '%pib%')  THEN 'PIB/Croissance'
    WHEN ((lower("public"."news"."description"))  LIKE '%énergies fossiles%' OR (lower("public"."news"."title"))  LIKE '%énergies fossiles%')  THEN 'Energies fossiles'
    WHEN ((lower("public"."news"."description"))  LIKE '%incendie%' OR (lower("public"."news"."title"))  LIKE '%incendie%')  THEN 'incendie'
    WHEN ((lower("public"."news"."description"))  LIKE '%canicule%' OR (lower("public"."news"."title"))  LIKE '%canicule%')  THEN 'canicule'
    WHEN ((lower("public"."news"."description"))  LIKE '%innondation%' OR (lower("public"."news"."title"))  LIKE '%innondation%')  THEN 'innondation'
    WHEN ((lower("public"."news"."description"))  LIKE '%aviation%' OR (lower("public"."news"."title"))  LIKE '%aviation%')  THEN 'aviation'
    WHEN ((lower("public"."news"."description"))  LIKE '%milliardaire%' OR (lower("public"."news"."title"))  LIKE '%milliardaire%')  THEN 'milliardaire'
    WHEN ((lower("public"."news"."description"))  LIKE '%capitalisme%' OR (lower("public"."news"."title"))  LIKE '%capitalisme%')  THEN 'capitalisme'
    WHEN ((lower("public"."news"."description"))  LIKE '%consommation%' OR (lower("public"."news"."title"))  LIKE '%consommation%')  THEN 'consommation'
    ELSE 'autre'
END AS group_description,
count(*) AS "count"
FROM "public"."news"
GROUP BY date_trunc('month', "public"."news"."date"), 2
ORDER BY date_trunc('month', "public"."news"."date") ASC
