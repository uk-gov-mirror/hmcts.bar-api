package uk.gov.hmcts.bar.api.data.service;


import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import uk.gov.hmcts.bar.api.data.enums.PaymentActionEnum;
import uk.gov.hmcts.bar.api.data.enums.PaymentStatusEnum;
import uk.gov.hmcts.bar.api.data.exceptions.InvalidActionException;
import uk.gov.hmcts.bar.api.data.exceptions.PaymentInstructionNotFoundException;
import uk.gov.hmcts.bar.api.data.model.CaseReference;
import uk.gov.hmcts.bar.api.data.model.CaseReferenceRequest;
import uk.gov.hmcts.bar.api.data.model.PaymentInstruction;
import uk.gov.hmcts.bar.api.data.model.PaymentInstructionActionRequest;
import uk.gov.hmcts.bar.api.data.model.PaymentInstructionOverview;
import uk.gov.hmcts.bar.api.data.model.PaymentInstructionRequest;
import uk.gov.hmcts.bar.api.data.model.PaymentInstructionSearchCriteriaDto;
import uk.gov.hmcts.bar.api.data.model.PaymentInstructionStatus;
import uk.gov.hmcts.bar.api.data.model.PaymentInstructionStatusReferenceKey;
import uk.gov.hmcts.bar.api.data.model.PaymentInstructionUpdateRequest;
import uk.gov.hmcts.bar.api.data.model.PaymentInstructionUserStats;
import uk.gov.hmcts.bar.api.data.model.PaymentReference;
import uk.gov.hmcts.bar.api.data.repository.PaymentInstructionRepository;
import uk.gov.hmcts.bar.api.data.repository.PaymentInstructionStatusRepository;
import uk.gov.hmcts.bar.api.data.repository.PaymentInstructionsSpecifications;
import uk.gov.hmcts.bar.api.data.utils.Util;


@Service
@Transactional
public class PaymentInstructionService {

    private static final Logger LOG = getLogger(PaymentInstructionService.class);

    public static final String SITE_ID = "BR01";
    private static final int PAGE_NUMBER = 0;
    private static final int MAX_RECORDS_PER_PAGE = 200;
    private PaymentInstructionRepository paymentInstructionRepository;
    private PaymentInstructionStatusRepository paymentInstructionStatusRepository;
    private PaymentReferenceService paymentReferenceService;
    private CaseReferenceService caseReferenceService;
    private final BarUserService barUserService;


	public PaymentInstructionService(PaymentReferenceService paymentReferenceService,
			CaseReferenceService caseReferenceService, PaymentInstructionRepository paymentInstructionRepository,
			BarUserService barUserService, PaymentInstructionStatusRepository paymentInstructionStatusRepository) {
		this.paymentReferenceService = paymentReferenceService;
		this.caseReferenceService = caseReferenceService;
		this.paymentInstructionRepository = paymentInstructionRepository;
		this.barUserService = barUserService;
		this.paymentInstructionStatusRepository = paymentInstructionStatusRepository;
	}

    public PaymentInstruction createPaymentInstruction(PaymentInstruction paymentInstruction) {
        String userId = barUserService.getCurrentUserId();

		PaymentReference nextPaymentReference = paymentReferenceService.getNextPaymentReferenceSequenceBySite(SITE_ID);
		paymentInstruction.setSiteId(SITE_ID);
		paymentInstruction.setDailySequenceId(nextPaymentReference.getDailySequenceId());
		paymentInstruction.setStatus(PaymentStatusEnum.DRAFT.dbKey());
		paymentInstruction.setUserId(userId);
		PaymentInstruction savedPaymentInstruction = paymentInstructionRepository.saveAndRefresh(paymentInstruction);
		savePaymentInstructionStatus(savedPaymentInstruction, userId);
		return savedPaymentInstruction;
    }

    public CaseReference createCaseReference(Integer paymentInstructionId, CaseReferenceRequest caseReferenceRequest) {

        Optional<PaymentInstruction> optionalPaymentInstruction = paymentInstructionRepository.findById(paymentInstructionId);
        PaymentInstruction existingPaymentInstruction = optionalPaymentInstruction
            .orElseThrow(() -> new PaymentInstructionNotFoundException(paymentInstructionId));

        CaseReference caseReference = new CaseReference(caseReferenceRequest.getCaseReference(), existingPaymentInstruction.getId());

        return caseReferenceService.saveCaseReference(caseReference);
    }


    public List<PaymentInstruction> getAllPaymentInstructions(PaymentInstructionSearchCriteriaDto paymentInstructionSearchCriteriaDto) {

        paymentInstructionSearchCriteriaDto.setSiteId(SITE_ID);
        PaymentInstructionsSpecifications paymentInstructionsSpecification = new PaymentInstructionsSpecifications(paymentInstructionSearchCriteriaDto);
        Sort sort = new Sort(Sort.Direction.DESC, "paymentDate");
        Pageable pageDetails = new PageRequest(PAGE_NUMBER, MAX_RECORDS_PER_PAGE, sort);

        return Lists.newArrayList(paymentInstructionRepository
            .findAll(paymentInstructionsSpecification.getPaymentInstructionsSpecification(), pageDetails)
            .iterator());
    }

    public PaymentInstruction getPaymentInstruction(Integer id) {
        return paymentInstructionRepository.findOne(id);
    }

    public void deletePaymentInstruction(Integer id) {
        try {
            paymentInstructionRepository.delete(id);
        } catch (EmptyResultDataAccessException erdae) {
            LOG.error("Resource not found: " + erdae.getMessage(), erdae);
            throw new PaymentInstructionNotFoundException(id);
        }

    }

    public PaymentInstruction submitPaymentInstruction(Integer id, PaymentInstructionUpdateRequest paymentInstructionUpdateRequest) {
    	String userId = barUserService.getCurrentUserId();
        Optional<PaymentInstruction> optionalPaymentInstruction = paymentInstructionRepository.findById(id);
        PaymentInstruction existingPaymentInstruction = optionalPaymentInstruction
            .orElseThrow(() -> new PaymentInstructionNotFoundException(id));
        String[] nullPropertiesNamesToIgnore = Util.getNullPropertyNames(paymentInstructionUpdateRequest);
        BeanUtils.copyProperties(paymentInstructionUpdateRequest, existingPaymentInstruction, nullPropertiesNamesToIgnore);
        existingPaymentInstruction.setUserId(userId);
        savePaymentInstructionStatus(existingPaymentInstruction, userId);
        return paymentInstructionRepository.saveAndRefresh(existingPaymentInstruction);
    }

    public PaymentInstruction updatePaymentInstruction(Integer id, PaymentInstructionRequest paymentInstructionRequest) {
    	String userId = barUserService.getCurrentUserId();
        Optional<PaymentInstruction> optionalPaymentInstruction = paymentInstructionRepository.findById(id);
        PaymentInstruction existingPaymentInstruction = optionalPaymentInstruction
            .orElseThrow(() -> new PaymentInstructionNotFoundException(id));
        String[] nullPropertiesNamesToIgnore = Util.getNullPropertyNames(paymentInstructionRequest);
        BeanUtils.copyProperties(paymentInstructionRequest, existingPaymentInstruction, nullPropertiesNamesToIgnore);
        existingPaymentInstruction.setUserId(userId);
        savePaymentInstructionStatus(existingPaymentInstruction, userId);
        return paymentInstructionRepository.saveAndRefresh(existingPaymentInstruction);
    }

    public List<PaymentInstruction> getAllPaymentInstructionsByCaseReference(String caseReference) {
        return paymentInstructionRepository.findByCaseReference(caseReference);
    }

    public PaymentInstruction actionPaymentInstruction(Integer id,
                                                       PaymentInstructionActionRequest paymentInstructionActionRequest) throws InvalidActionException {
        if (PaymentActionEnum.getPaymentActionEnum(paymentInstructionActionRequest.getAction().trim()) == null) {
            throw new InvalidActionException("Invalid action string: " + paymentInstructionActionRequest.getAction());
        }
        Optional<PaymentInstruction> optionalPaymentInstruction = paymentInstructionRepository.findById(id);
        PaymentInstruction existingPaymentInstruction = optionalPaymentInstruction
            .orElseThrow(() -> new PaymentInstructionNotFoundException(id));
        String[] nullPropertiesNamesToIgnore = Util.getNullPropertyNames(paymentInstructionActionRequest);
        BeanUtils.copyProperties(paymentInstructionActionRequest, existingPaymentInstruction,
            nullPropertiesNamesToIgnore);
        return paymentInstructionRepository.saveAndRefresh(existingPaymentInstruction);
    }
    
	@SuppressWarnings("unchecked")
	public MultiMap getPaymentInstructionStats(String userRole) {
		List<PaymentInstructionOverview> paymentInstructionStatsList = paymentInstructionStatusRepository
				.getPaymentOverviewStats(userRole);
		MultiMap paymentInstructionStatsUserMap = new MultiValueMap();
		paymentInstructionStatsList.forEach(pis -> paymentInstructionStatsUserMap.put(pis.getBarUserId(), pis));
		List<PaymentInstructionUserStats> paymentInstructionInPAList = paymentInstructionStatusRepository
				.getPaymentInstructionsPendingApprovalByUserGroup(userRole);
		paymentInstructionInPAList.forEach(pius -> {
			String user = pius.getBarUserId();
			pius.setBarUserId(null);
			paymentInstructionStatsUserMap.put(user, pius);
		});
		return paymentInstructionStatsUserMap;
	}

	private void savePaymentInstructionStatus(PaymentInstruction pi, String userId) {
		PaymentInstructionStatusReferenceKey pisrKey = new PaymentInstructionStatusReferenceKey(pi.getId(),
				pi.getStatus());
		PaymentInstructionStatus pis = new PaymentInstructionStatus(pisrKey, userId);
		paymentInstructionStatusRepository.save(pis);
	}


}
