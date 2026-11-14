# Radio Buttons Test Spec

## Application overview
The Radio Buttons page on https://the-internet.herokuapp.com/radio_buttons (reached from the landing page via the "Radio Buttons" link, `a[href='/radio_buttons']`). It has two groups of radio buttons inside `form#radio-buttons`:
- "Favorite color": Red, Blue (checked by default), Green.
- "Favorite sport": Football, Tennis, Hockey (not available, disabled). No sport is selected by default.

A result line under the form (`p#result`) is updated by script on every change and shows the current selection.

This spec was written from a live exploration of the site (locators verified to resolve to exactly one element). No page object, test or CSV exists yet.

## Seed
Starting URL: `https://the-internet.herokuapp.com/` (base URL, opened by `launchBrowser(browser)`), then click the "Radio Buttons" link.

## Page elements
| Element | Locator (By) | Suggested page object | Notes |
|---------|--------------|-----------------------|-------|
| Radio Buttons link (landing page) | `By.cssSelector("a[href='/radio_buttons']")` or link text `Radio Buttons` | `HomePage` (add `clickRadioButtonsButton()`) | Returns `RadioButtonsPage` |
| Form | `By.id("radio-buttons")` | `RadioButtonsPage` | Contains both groups |
| Red radio | `By.id("red")` | `RadioButtonsPage` | `name=color`, `value=red` |
| Blue radio | `By.id("blue")` | `RadioButtonsPage` | `name=color`, `value=blue`, checked by default |
| Green radio | `By.id("green")` | `RadioButtonsPage` | `name=color`, `value=green` |
| Football radio | `By.cssSelector("input[name='sport'][value='football']")` | `RadioButtonsPage` | No id, no `for` label |
| Tennis radio | `By.cssSelector("input[name='sport'][value='tennis']")` | `RadioButtonsPage` | No id, no `for` label |
| Hockey radio | `By.cssSelector("input[name='sport'][value='hockey']")` | `RadioButtonsPage` | Disabled, label "Hockey (not available)" |
| Result text | `By.id("result")` | `RadioButtonsPage` | Read with `getText` |

Sport radios have no ids, so the CSS selectors above are the shortest unique locators.

## Observed result text
| State | `#result` text |
|-------|----------------|
| Initial load | `Color: blue, Sport: none` |
| After clicking Green | `Color: green, Sport: none` (observed) |
| After clicking Green then Tennis | `Color: green, Sport: tennis` (observed) |

Format is `Color: <value>, Sport: <value>` where values are the lowercase `value` attributes of the checked radios (`none` if nothing is checked).

## Scenarios

### 1. Radio Buttons
**Suggested names:** test class `TestRadioButtonsTest`, page object `RadioButtonsPage`, data folder `Radio Button Cases`
**Severity:** NORMAL
**Data columns:** `Color`, `Sport`, `Expected Result` (the framework adds `browser` as the first parameter). Keep expected values in the CSV.

#### 1.1 Select green and tennis, verify the displayed values
**Steps:**
1. Launch the browser on the base URL (`https://the-internet.herokuapp.com/`).
2. Click the "Radio Buttons" link on the landing page.
   - Expected: the Radio Buttons page is shown; `#result` reads `Color: blue, Sport: none`.
3. Select the green radio button (`#green`).
   - Expected: green is selected and blue is no longer selected.
4. Select the tennis radio button (`input[name='sport'][value='tennis']`).
   - Expected: tennis is selected.
5. Read the result text (`#result`).
   - Expected: it is exactly `Color: green, Sport: tennis`.

**Final verification:**
- Colour shown: `green`; sport shown: `tennis`.
- Full text of `#result`: `Color: green, Sport: tennis`.
- `isSelected` is true for green and tennis, false for blue.

**Data rows:**
| Color | Sport | Expected Result |
|-------|-------|-----------------|
| green | tennis | Color: green, Sport: tennis |

Data-driven notes: the test runs once per CSV row per enabled browser. The page object can build the sport locator from the data value, so more rows (red/football, and so on) work without code changes.

## Not yet automated (candidate scenarios)
- Default state: blue selected and `Color: blue, Sport: none` on load.
- Hockey radio is disabled and cannot be selected (`isEnabled` false).
- Changing the selection (green then red) updates the result and keeps only one colour selected.
