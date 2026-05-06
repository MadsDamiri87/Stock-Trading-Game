Goes for all the following preconditions:  
P1 = Application is launched, market is initialized, the single portfolio is loaded

---

## SC-UC1-01 - Successful purchase of available stock

**Test Case ID:** SC-UC1-01  
**Traceability:** UC1 – Buy Stock  
**Test Case Name:** Successful purchase of available stocks.  
**Precondition:** P1, enough balance, TSLA stock exists and is not bankrupt.  
**Actor:** Trader/user

| Step | Action             | Input | Expected                          | Actual      | Pass/Fail |
| ---- | ------------------ | ----- | --------------------------------- |-------------|-----------|
| 1    | Open Buy page      | -     | Page opens                        | As Expected | Pass      |
| 2    | Select TSLA        | TSLA  | Stock shown                       | As Expected | Pass      |
| 3    | Enter shares       | 1     | Accepted                          | As Expected | Pass      |
| 4    | Click “Buy Stock”  | -     | Purchase complete                 | As Expected | Pass      |
| 5    | Check owned shares | -     | 1 stock is added to Owned Shares. | As Expected | Pass      |
| 6    | Check portfolio    | -     | Balance reduced, shares +1        | As Expected | Pass      |

---

## SC-UC1-02 - Fail: insufficient funds

**Test Case ID:** SC-UC1-02  
**Traceability:** UC1 – Buy Stock  
**Test Case Name:** Purchase fails due to insufficient funds.  
**Precondition:** P1, available balance is lower than the total cost of the selected purchase.  
**Actor:** Trader/user

| Step | Action          | Input | Expected           | Actual      | Pass/Fail |
| ---- | --------------- | ----- | ------------------ |-------------|-----------|
| 1    | Open Buy page   | -     | Page opens         | As expected | Pass      |
| 2    | Select stock    | MSFT  | Stock shown        | As expected | Pass      |
| 3    | Enter shares    | 100   | Accepted           | As expected | Pass      |
| 4    | Click Buy       | -     | Rejected           | As expected | Pass      |
| 5    | Check message   | -     | Insufficient funds | As expected | Pass      |
| 6    | Check portfolio | -     | No change          | As expected | Pass      |

---

## SC-UC2-01 - Successful sale

**Test Case ID:** SC-UC2-01  
**Traceability:** UC2 – Sell Stock  
**Test Case Name:** Successful sale of owned stock.  
**Precondition:** P1, Trader owns exactly 1 TSLA  
**Actor:** Trader/user

| Step | Action          | Input | Expected                                | Actual      | Pass/Fail |
| ---- | --------------- | ----- | --------------------------------------- |-------------|-----------|
| 1    | Open Sell page  | -     | Page opens                              | As expected | Pass      |
| 2    | Select TSLA     | TSLA  | Stock shown                             | As expected | Pass      |
| 3    | Enter shares    | 1     | Accepted                                | As expected | Pass      |
| 4    | Click Sell      | -     | Sale complete                           | As expected | Pass      |
| 5    | Check message   | -     | System shows owned TSLA shares minus 0. | As expected | Pass      |
| 6    | Check portfolio | -     | Balance increased                       | As expected | Pass      |

---

## SC-UC2-02 - Fail: too many shares

**Test Case ID:** SC-UC2-02  
**Traceability:** UC2 – Sell Stock  
**Test Case Name:** Sale fails because investor does not own enough shares.  
**Precondition:** P1, Ownes fewer shares than selling,  
**Actor:** Investor

| Step | Action          | Input | Expected                  | Actual      | Pass/Fail |
| ---- | --------------- | ----- | ------------------------- |-------------|-----------|
| 1    | Open Sell page  | -     | Page opens                | As expected | Pass      |
| 2    | Select TSLA     | TSLA  | Stock shown               | As expected | Pass      |
| 3    | Enter shares    | 10    | Accepted                  | As expected | Pass      |
| 4    | Click Sell      | -     | Rejected                  | As expected | Pass      |
| 5    | Check message   | -     | Not enough shares to sell | As expected | Pass      |
| 6    | Check portfolio | -     | No change                 | As expected | Pass      |
