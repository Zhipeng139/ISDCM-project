# Permission Languages: ODRL

ODRL (Open Digital Rights Language) is a permission language that is used to express digital rights. It is a W3C standard. ODRL then will be used in PEP (Policy Enforcement Point) to evaluate permissions, to guarantee that the digital rights are respected.


The ODRL Information Model has the following classes:

- Policy: A policy is a set of rules that define the Permissions and/or Duties and/or Prohibitions.
    - Set: A set is a collection of Permissions, Duties, and Prohibitions.
    - Offer: A offer is a policy that is offered to a assignee Party.
    - Agreement: An agreement is a policy that is granted to a assignee Party.
- Asset: An asset is a digital resource that is protected by a policy.
- Party: A party is an entity that is subject to the rights and obligations defined in a policy.
- Action: An action is a verb that represents the act of using an asset.
- Rule: A rule is a statement that defines the conditions under which a Permission, Duty, or Prohibition is granted.
- Constraint/LogicalConstraint: A constraint is a statement that defines the conditions under which a rule is granted.

ODRL example:


PolicyDefinition:

```json
{
   "@context":[
      "https://w3id.org/edc/connector/management/v0.0.1"
   ],
   "@type":"PolicyDefinition",
   "@id":"require-inforcedate-duration",
   "policy":{
      "@type":"Set",
      "permission":[
         {
            "action":"use",
            "constraint":[
               {
                  "and":[
                     {
                        "leftOperand":"edc:inForceDate",
                        "operator":"gt",
                        "rightOperand":{
                           "@value":"contractAgreement+0s",
                           "@type":"edc:inForceDate:dateExpression"
                        }
                     },
                     {
                        "leftOperand":"edc:inForceDate",
                        "operator":"lt",
                        "rightOperand":{
                           "@value":"contractAgreement+120s",
                           "@type":"edc:inForceDate:dateExpression"
                        }
                     }
                  ]
               }
            ]
         }
      ]
   }
}
```

## What problem does it solve?

In the digital transformation era, manually reading through voluminous legal contracts to determine copyright is extremely inefficient. ODRL exists to make rights **machine-readable**.

| **Traditional Method** | **ODRL Method** |
| :--- | :--- |
| **Legal documents** (PDF/Paper) that only lawyers can understand. | **Structured data** (JSON-LD/XML) that computer programs can parse directly. |
| **Slow licensing process** requiring manual human review. | **Automated licensing** where systems can automatically determine access based on policies. |
| **Vague rules.** | **Rigorous logic** defining precise concepts such as Assets, Assignees, Actions, and Constraints. |

## Limitation of ODRL

A primary limitation of this type of restriction is the inability to enforce post access permissions such as non-commercial use or prohibition of redistribution. Because ODRL can only secure the initial point of access (by verifying if a user possesses the required credentials defined in the permission language), it cannot control how the data or assets are used once they have been retrieved. To prevent these risks, ODRL based technical controls are usually complemented by legal contract or agreements to ensure accountability beyond the point of data delivery.
