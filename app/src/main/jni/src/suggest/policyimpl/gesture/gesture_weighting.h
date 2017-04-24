//
// Created by msj on 2017/3/9.
//

#ifndef GOOGLEKEYBOARDV7_GESTURE_WEIGHTING_H
#define GOOGLEKEYBOARDV7_GESTURE_WEIGHTING_H



#include "defines.h"
#include "suggest/core/dicnode/dic_node_utils.h"
#include "suggest/core/dictionary/error_type_utils.h"
#include "suggest/core/layout/touch_position_correction_utils.h"
#include "suggest/core/layout/proximity_info.h"
#include "suggest/core/policy/weighting.h"
#include "suggest/core/session/dic_traverse_session.h"
#include "suggest/policyimpl/typing/scoring_params.h"
#include "suggest/policyimpl/gesture/scoring_params_g.h"
#include "utils/char_utils.h"

namespace kikaime {

    class DicNode;
    struct DicNode_InputStateG;
    class MultiBigramMap;

    class GestureWeighting : public Weighting {
    public:
        static const GestureWeighting *getInstance() { return &sInstance; }

    protected:
        float getTerminalSpatialCost(const DicTraverseSession *const traverseSession,
                                     const DicNode *const dicNode) const {
            const int point0index = dicNode->getInputIndex(0);
            const int tmpInputSize = traverseSession->getInputSize();
            float cost = 0;
            for (int i = point0index; i < tmpInputSize; i++) {
                cost += traverseSession->getProximityInfoState(0)->getProbability(i, NOT_AN_INDEX);
            }
            if (point0index > tmpInputSize) {
                cost += (point0index - tmpInputSize) * ScoringParamsG::DISTANCE_WEIGHT_EXCEEDING_INPUT_SIZE;
            }

            return cost;
        }

        float getTerminalLanguageCost(const DicTraverseSession *const traverseSession,
                                      const DicNode *const dicNode, const float dicNodeLanguageImprobability) const {
            return dicNodeLanguageImprobability * ScoringParamsG::DISTANCE_WEIGHT_LANGUAGE;
        }

        float getCompletionCost(const DicTraverseSession *const traverseSession,
                                const DicNode *const dicNode) const {
            // The auto completion starts when the input index is same as the input size
            const bool firstCompletion = dicNode->getInputIndex(0)
                                         == traverseSession->getInputSize();
            // TODO: Change the cost for the first completion for the gesture?
            const float cost = firstCompletion ? ScoringParamsG::COST_FIRST_COMPLETION
                                               : ScoringParamsG::COST_COMPLETION;
            return cost;
        }

        float getSkipCost(const DicTraverseSession *const traverseSession,
                          const DicNode *const dicNode) const {
            const int pointIndex = dicNode->getInputIndex(0);

            float probability = traverseSession->getProximityInfoState(0)->getProbability(pointIndex, NOT_AN_INDEX);

            return probability;
        }

        float getOmissionCost(const DicNode *const parentDicNode, const DicNode *const dicNode) const {
            return (dicNode->isSameNodeCodePoint(parentDicNode))
                   ? ScoringParamsG::OMISSION_COST_SAME_CHAR
                   : ScoringParamsG::OMISSION_COST;
        }

        float getMatchedCost(const DicTraverseSession *const traverseSession,
                             const DicNode *const dicNode, DicNode_InputStateG *inputStateG) const {
            const int pointIndex = dicNode->getInputIndex(0);
            const int baseChar = CharUtils::toBaseLowerCase(dicNode->getNodeCodePoint());
            const int keyId = traverseSession->getProximityInfo()->getKeyIndexOf(baseChar);
            const float probability = traverseSession-> getProximityInfoState(0)->getProbability(pointIndex, keyId);

            return probability;
        }

        bool isProximityDicNode(const DicTraverseSession *const traverseSession,
                                const DicNode *const dicNode) const {
            return false;
        }

        float getTranspositionCost(const DicTraverseSession *const traverseSession,
                                   const DicNode *const parentDicNode, const DicNode *const dicNode) const {
            return 0;
        }

        float getInsertionCost(const DicTraverseSession *const traverseSession,
                               const DicNode *const parentDicNode, const DicNode *const dicNode) const {
            return 0;
        }

        float getSpaceOmissionCost(const DicTraverseSession *const traverseSession,
                                   const DicNode *const dicNode, DicNode_InputStateG *inputStateG) const {
            return 0;
        }

        float getNewWordBigramLanguageCost(const DicTraverseSession *const traverseSession,
                                           const DicNode *const dicNode,
                                           MultiBigramMap *const multiBigramMap) const {
            return 0;
        }

        float getTerminalInsertionCost(const DicTraverseSession *const traverseSession,
                                       const DicNode *const dicNode) const {
            return 0;
        }

        AK_FORCE_INLINE bool needsToNormalizeCompoundDistance() const {
            return false;
        }

        AK_FORCE_INLINE float getAdditionalProximityCost() const {
            return 0;
        }

        AK_FORCE_INLINE float getSubstitutionCost() const {
            return 0;
        }

        AK_FORCE_INLINE float getSpaceSubstitutionCost(const DicTraverseSession *const traverseSession,
                                                       const DicNode *const dicNode) const {
            return 0;
        }

        ErrorTypeUtils::ErrorType getErrorType(const CorrectionType correctionType,
                                               const DicTraverseSession *const traverseSession,
                                               const DicNode *const parentDicNode, const DicNode *const dicNode) const;

    private:
        DISALLOW_COPY_AND_ASSIGN(GestureWeighting);
        static const GestureWeighting sInstance;

        GestureWeighting() {}
        ~GestureWeighting() {}
    };
} // namespace kikaime


#endif //GOOGLEKEYBOARDV7_GESTURE_WEIGHTING_H
