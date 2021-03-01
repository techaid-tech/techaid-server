package ju.ma.app

import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.PagingAndSortingRepository

interface VolunteerRepository : PagingAndSortingRepository<Volunteer, Long>, QuerydslPredicateExecutor<Volunteer> {
    fun findByEmail(email: String): Volunteer?
}

interface ImageRepository : PagingAndSortingRepository<KitImage, Long>, QuerydslPredicateExecutor<KitImage>

interface DonorRepository : PagingAndSortingRepository<Donor, Long>, QuerydslPredicateExecutor<Donor> {
    fun findByEmail(email: String): Donor?
    fun findByPhoneNumber(phone: String): Donor?
}

interface KitStatusCount {
    val status: KitStatus
    val count: Long
}

interface KitTypeCount {
    val type: KitType
    val count: Long
}

interface KitRepository : PagingAndSortingRepository<Kit, Long>, QuerydslPredicateExecutor<Kit> {
    @Query(
        """
        SELECT k.status AS status, count(*) AS count from Kit k where k.archived != 'Y' group by k.status 
    """
    )
    fun statusCount(): List<KitStatusCount>

    @Query(
        """
        SELECT k.type AS type, count(*) AS count from Kit k where k.archived != 'Y' group by k.type
    """
    )
    fun typeCount(): List<KitTypeCount>
}

interface RequestCount {
    val phones: Long
    val laptops: Long
    val tablets: Long
    val allInOnes: Long
    val desktops: Long
    val other: Long
    val chromebooks: Long
    val commsDevices: Long
}

interface OrganisationRepository : PagingAndSortingRepository<Organisation, Long>,
    QuerydslPredicateExecutor<Organisation> {
    @Query(
        """
        SELECT
            coalesce(sum(src.phones),0) AS phones,
            coalesce(sum(src.laptops),0) AS laptops,
            coalesce(sum(src.tablets),0) AS tablets,
            coalesce(sum(src.allInOnes),0) AS allInOnes,
            coalesce(sum(src.desktops),0) AS desktops,
            coalesce(sum(src.other),0) AS other,
            coalesce(sum(src.chromebooks),0) AS chromebooks,
            coalesce(sum(src.commsDevices),0) AS commsDevices
        FROM (
          SELECT 
              id,
              coalesce((attributes->'request'->'phones')\:\:int +  (attributes->'alternateRequest'->'phones')\:\:int, 0) as phones,
              coalesce((attributes->'request'->'laptops')\:\:int +  (attributes->'alternateRequest'->'laptops')\:\:int, 0) as laptops,
              coalesce((attributes->'request'->'tablets')\:\:int +  (attributes->'alternateRequest'->'tablets')\:\:int, 0) as tablets,
              coalesce((attributes->'request'->'allInOnes')\:\:int +  (attributes->'alternateRequest'->'allInOnes')\:\:int, 0) as allInOnes,
              coalesce((attributes->'request'->'desktops')\:\:int +  (attributes->'alternateRequest'->'desktops')\:\:int, 0) as desktops,
              coalesce((attributes->'request'->'other')\:\:int +  (attributes->'alternateRequest'->'other')\:\:int, 0) as other,
              coalesce((attributes->'request'->'chromebooks')\:\:int +  (attributes->'alternateRequest'->'chromebooks')\:\:int, 0) as chromebooks,
              coalesce((attributes->'request'->'commsDevices')\:\:int +  (attributes->'alternateRequest'->'commsDevices')\:\:int, 0) as commsDevices 
          FROM organisations org
          WHERE org.archived != 'Y' 
        ) AS src
    """,
        nativeQuery = true
    )
    fun requestCount(): RequestCount
}

interface EmailTemplateRepository : PagingAndSortingRepository<EmailTemplate, Long>,
    QuerydslPredicateExecutor<EmailTemplate>
