select group_description, "date", count as nombre_de_reportage_mois
FROM (
    SELECT date_trunc('month', "public"."news"."date") AS "date",
    CASE
    --    WHEN ((lower("public"."news"."description"))  LIKE '%covid%' OR (lower("public"."news"."title"))  LIKE '%covid%') THEN 'covid'
        WHEN ((lower("public"."news"."description"))  LIKE '%climatique%' OR (lower("public"."news"."title"))  LIKE '%climatique%')  THEN 'rechauffement climatique'
        WHEN ((lower("public"."news"."description"))  LIKE '%giec%' OR (lower("public"."news"."title"))  LIKE '%giec%')  THEN 'rechauffement climatique'
        WHEN ((lower("public"."news"."description"))  LIKE '%COP2%' OR (lower("public"."news"."title"))  LIKE '%COP2%')  THEN 'rechauffement climatique'
        WHEN ((lower("public"."news"."description"))  LIKE '%pib%' OR (lower("public"."news"."title"))  LIKE '%pib%' OR (lower("public"."news"."title"))  LIKE '%croissance%' OR (lower("public"."news"."title"))  LIKE '%croissance%') THEN 'PIB/Croissance'
        WHEN ((lower("public"."news"."description"))  LIKE '%charbon%' OR (lower("public"."news"."title"))  LIKE '%charbon%')  THEN 'Charbon'
        WHEN ((lower("public"."news"."description"))  LIKE '%incendie%' OR (lower("public"."news"."title"))  LIKE '%incendie%')  THEN 'incendie'
        WHEN ((lower("public"."news"."description"))  LIKE '%canicule%' OR (lower("public"."news"."title"))  LIKE '%canicule%')  THEN 'canicule'
        WHEN ((lower("public"."news"."description"))  LIKE '%innondation%' OR (lower("public"."news"."title"))  LIKE '%innondation%')  THEN 'innondation'
        WHEN ((lower("public"."news"."description"))  LIKE '%crue%' OR (lower("public"."news"."title"))  LIKE '%crue%')  THEN 'innondation'
        WHEN ((lower("public"."news"."description"))  LIKE '%aviation%' OR (lower("public"."news"."title"))  LIKE '%aviation%')  THEN 'aviation'
        WHEN ((lower("public"."news"."description"))  LIKE '%airbus%' OR (lower("public"."news"."title"))  LIKE '%airbus%')  THEN 'aviation'
        WHEN ((lower("public"."news"."description"))  LIKE '%milliardaire%' OR (lower("public"."news"."title"))  LIKE '%milliardaire%')  THEN 'milliardaire'
        WHEN ((lower("public"."news"."description"))  LIKE '%consommation%' OR (lower("public"."news"."title"))  LIKE '%consommation%')  THEN 'consommation'
        ELSE 'autre'
    END AS group_description,
    count(*) AS "count"
    FROM "public"."news"
    GROUP BY date_trunc('month', "public"."news"."date"), 2
    ) test
where group_description != 'autre'
ORDER BY "date"
