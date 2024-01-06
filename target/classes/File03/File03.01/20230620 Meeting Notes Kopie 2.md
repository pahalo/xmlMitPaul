# Meeting Notes - 20.06.2023

<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

## Finished ToDo's
- ~~ToDo Steffen: provide jhove result files for different images~~
- ~~ToDo Steffen: Extend the validation plugin to allow using properties as requirements (e.g. expected resolution and color depth)~~
- ~~ToDo Steffen: Make sure the scrolling works after an Ajax request as well~~
- ~~ToDo Yale: Clarify color depth to be 16 or 24 bit vs. delivered samples with 8 bit~~
- ~~ToDo Steffen:_ split color depth into 3 values~~
  - this is configured like this now:

  ```xml
        <!--Check color depth RED -->
        <check>
            <xpath>string(//mix:bitsPerSampleValue[1])</xpath>
            <wanted>8</wanted>
            <error_message> Check color depth for "${image}": Expected value "${wanted}" for RED, 
            but found value "${found}".</error_message>
        </check>
        <!--Check color depth GREEN -->
        <check>
            <xpath>string(//mix:bitsPerSampleValue[2])</xpath>
            <wanted>8</wanted>
            <error_message> Check color depth for "${image}": Expected value "${wanted}" for GREEN, 
            but found value "${found}".</error_message>
        </check>
        <!--Check color depth BLUE -->
        <check>
            <xpath>string(//mix:bitsPerSampleValue[3])</xpath>
            <wanted>8</wanted>
            <error_message> Check color depth for "${image}": Expected value "${wanted}" for BLUE, 
            but found value "${found}".</error_message>
        </check>
    ```
- ~~ToDo Steffen: adapt configuration to remove `PPI` from there and use it in tif validation~~
- ~~ToDo Steffen: check if resolution can be configured with minimum value only~~
  - A maxium value needs to be defined like this:

  ```xml
  <!--Check for resolution (number or range) -->
  <integrated_check name="resolution_check">
      <mix_uri>http://www.loc.gov/mix/v20</mix_uri>
      <wanted>(process.PPI)-800</wanted>
      <error_message> Check resolution for "${image}": Expected value "${wanted}", 
      but found value "${found}".</error_message>
  </integrated_check>
  ```


<!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
## Open ToDo's


### Validation Checks
- _ToDo Yale:_ provide a list of images and combined validation expectations 
- _ToDo Yale:_ TEST TEST TEST!!! 

### Clarification of AEON QUEUE IDs
- _ToDo Trip:_ come back with the final Aeon IDs
  - AEON Update - Arrive in Preventive Conservation: `1158`
  - AEON Update - Arrive in DRMS: `1159`
  - AEON Update - Files are availabe (?): `xyz`

### Not-working item:
- ID: `288061_null_0001`
- `Condition Assessment` does not show any properties


### Further Workflow setup
- Workflow shall look like here in [the Google Docs here](https://docs.google.com/document/d/1EyNmBp1vdpF7o69r9h30rK3_yyZG8S7-yEgB_7Sckh0/edit#heading=h.wcevwwfajs74)
- _ToDo Steffen:_ Try to move on with the Google Doc and document the following steps
- _ToDo Steffen:_ Try to implement as much as possible (based on step `Material Assessment`) the Edinburgh kind of workflow (scan, photos, scan & photos, throw away, av)

## Sample identifiers
* 286670 - (DRMS) Book & Paper
* 286528 - Audio Recording (DRMS) (also includes a restriction note in Aeon)
* 287365 - Video (DRMS)
* 287366 - Film (DRMS)
* 287367 - Transmissives