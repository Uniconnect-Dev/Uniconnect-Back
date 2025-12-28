package com.uniConnect.campaign.repository;

import com.uniConnect.campaign.entity.CampaignTarget;
import com.uniConnect.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignTargetRepository extends JpaRepository<CampaignTarget, Long> {

    List<CampaignTarget> findByCampaign_Company(Company company);
}