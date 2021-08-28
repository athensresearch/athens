import React from 'react';

export const parameters = {
  actions: { argTypesRegex: "^on[A-Z].*" },
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
}

const storyStyle = `
.docs-story {
  --background-color---opacity-high: hsla(0, 0%, 10.196078431372548%, 0.75);
  --background-color---opacity-higher: hsla(0, 0%, 10.196078431372548%, 0.85);
  --background-color---opacity-low: hsla(0, 0%, 10.196078431372548%, 0.25);
  --background-color---opacity-lower: hsla(0, 0%, 10.196078431372548%, 0.1);
  --background-color---opacity-med: hsla(0, 0%, 10.196078431372548%, 0.5);
  --background-color: #1A1A1A;
  --background-minus-1---opacity-high: hsla(0, 0%, 8.235294117647058%, 0.75);
  --background-minus-1---opacity-higher: hsla(0, 0%, 8.235294117647058%, 0.85);
  --background-minus-1---opacity-low: hsla(0, 0%, 8.235294117647058%, 0.25);
  --background-minus-1---opacity-lower: hsla(0, 0%, 8.235294117647058%, 0.1);
  --background-minus-1---opacity-med: hsla(0, 0%, 8.235294117647058%, 0.5);
  --background-minus-1: #151515;
  --background-minus-2---opacity-high: hsla(0, 0%, 6.6667%, 0.75);
  --background-minus-2---opacity-higher: hsla(0, 0%, 6.6667%, 0.85);
  --background-minus-2---opacity-low: hsla(0, 0%, 6.6667%, 0.25);
  --background-minus-2---opacity-lower: hsla(0, 0%, 6.6667%, 0.1);
  --background-minus-2---opacity-med: hsla(0, 0%, 6.6667%, 0.5);
  --background-minus-2: #111;
  --background-plus-1---opacity-high: hsla(0, 0%, 13.333%, 0.75);
  --background-plus-1---opacity-higher: hsla(0, 0%, 13.333%, 0.85);
  --background-plus-1---opacity-low: hsla(0, 0%, 13.333%, 0.25);
  --background-plus-1---opacity-lower: hsla(0, 0%, 13.333%, 0.1);
  --background-plus-1---opacity-med: hsla(0, 0%, 13.333%, 0.5);
  --background-plus-1: #222;
  --background-plus-2---opacity-high: hsla(0, 0%, 20%, 0.75);
  --background-plus-2---opacity-higher: hsla(0, 0%, 20%, 0.85);
  --background-plus-2---opacity-low: hsla(0, 0%, 20%, 0.25);
  --background-plus-2---opacity-lower: hsla(0, 0%, 20%, 0.1);
  --background-plus-2---opacity-med: hsla(0, 0%, 20%, 0.5);
  --background-plus-2: #333;
  --body-text-color---opacity-high: hsla(0, 0%, 66.666%, 0.75);
  --body-text-color---opacity-higher: hsla(0, 0%, 66.666%, 0.85);
  --body-text-color---opacity-low: hsla(0, 0%, 66.666%, 0.25);
  --body-text-color---opacity-lower: hsla(0, 0%, 66.666%, 0.1);
  --body-text-color---opacity-med: hsla(0, 0%, 66.666%, 0.5);
  --body-text-color: #AAA;
  --border-color---opacity-high: hsla(0, 0%, 0%, 0.75);
  --border-color---opacity-higher: hsla(0, 0%, 0%, 0.85);
  --border-color---opacity-low: hsla(0, 0%, 0%, 0.25);
  --border-color---opacity-lower: hsla(0, 0%, 0%, 0.1);
  --border-color---opacity-med: hsla(0, 0%, 0%, 0.5);
  --border-color: hsla(32, 81%, 90%, 0.08);
  --confirmation-color---opacity-high: hsla(133.43283582089555, 73.62637362637363%, 35.68627450980392%, 0.75);
  --confirmation-color---opacity-higher: hsla(133.43283582089555, 73.62637362637363%, 35.68627450980392%, 0.85);
  --confirmation-color---opacity-low: hsla(133.43283582089555, 73.62637362637363%, 35.68627450980392%, 0.25);
  --confirmation-color---opacity-lower: hsla(133.43283582089555, 73.62637362637363%, 35.68627450980392%, 0.1);
  --confirmation-color---opacity-med: hsla(133.43283582089555, 73.62637362637363%, 35.68627450980392%, 0.5);
  --confirmation-color: #189E36;
  --error-color---opacity-high: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.75);
  --error-color---opacity-higher: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.85);
  --error-color---opacity-low: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.25);
  --error-color---opacity-lower: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.1);
  --error-color---opacity-med: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.5);
  --error-color: #fd5243;
  --graph-control-bg---opacity-high: hsla(0, 0%, 15.294117647058824%, 0.75);
  --graph-control-bg---opacity-higher: hsla(0, 0%, 15.294117647058824%, 0.85);
  --graph-control-bg---opacity-low: hsla(0, 0%, 15.294117647058824%, 0.25);
  --graph-control-bg---opacity-lower: hsla(0, 0%, 15.294117647058824%, 0.1);
  --graph-control-bg---opacity-med: hsla(0, 0%, 15.294117647058824%, 0.5);
  --graph-control-bg: #272727;
  --graph-control-color---opacity-high: hsla(0, 0%, 0%, 0.75);
  --graph-control-color---opacity-higher: hsla(0, 0%, 0%, 0.85);
  --graph-control-color---opacity-low: hsla(0, 0%, 0%, 0.25);
  --graph-control-color---opacity-lower: hsla(0, 0%, 0%, 0.1);
  --graph-control-color---opacity-med: hsla(0, 0%, 0%, 0.5);
  --graph-control-color: white;
  --graph-link-normal---opacity-high: hsla(0, 0%, 19.607843137254903%, 0.75);
  --graph-link-normal---opacity-higher: hsla(0, 0%, 19.607843137254903%, 0.85);
  --graph-link-normal---opacity-low: hsla(0, 0%, 19.607843137254903%, 0.25);
  --graph-link-normal---opacity-lower: hsla(0, 0%, 19.607843137254903%, 0.1);
  --graph-link-normal---opacity-med: hsla(0, 0%, 19.607843137254903%, 0.5);
  --graph-link-normal: #323232;
  --graph-node-hlt---opacity-high: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.75);
  --graph-node-hlt---opacity-higher: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.85);
  --graph-node-hlt---opacity-low: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.25);
  --graph-node-hlt---opacity-lower: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.1);
  --graph-node-hlt---opacity-med: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.5);
  --graph-node-hlt: #FBBE63;
  --graph-node-normal---opacity-high: hsla(0, 0%, 56.470588235294116%, 0.75);
  --graph-node-normal---opacity-higher: hsla(0, 0%, 56.470588235294116%, 0.85);
  --graph-node-normal---opacity-low: hsla(0, 0%, 56.470588235294116%, 0.25);
  --graph-node-normal---opacity-lower: hsla(0, 0%, 56.470588235294116%, 0.1);
  --graph-node-normal---opacity-med: hsla(0, 0%, 56.470588235294116%, 0.5);
  --graph-node-normal: #909090;
  --header-text-color---opacity-high: hsla(0, 0%, 72.94117647058823%, 0.75);
  --header-text-color---opacity-higher: hsla(0, 0%, 72.94117647058823%, 0.85);
  --header-text-color---opacity-low: hsla(0, 0%, 72.94117647058823%, 0.25);
  --header-text-color---opacity-lower: hsla(0, 0%, 72.94117647058823%, 0.1);
  --header-text-color---opacity-med: hsla(0, 0%, 72.94117647058823%, 0.5);
  --header-text-color: #BABABA;
  --highlight-color---opacity-high: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.75);
  --highlight-color---opacity-higher: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.85);
  --highlight-color---opacity-low: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.25);
  --highlight-color---opacity-lower: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.1);
  --highlight-color---opacity-med: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.5);
  --highlight-color: #FBBE63;
  --link-color---opacity-high: hsla(203.8775510204082, 80.32786885245902%, 52.156862745098046%, 0.75);
  --link-color---opacity-higher: hsla(203.8775510204082, 80.32786885245902%, 52.156862745098046%, 0.85);
  --link-color---opacity-low: hsla(203.8775510204082, 80.32786885245902%, 52.156862745098046%, 0.25);
  --link-color---opacity-lower: hsla(203.8775510204082, 80.32786885245902%, 52.156862745098046%, 0.1);
  --link-color---opacity-med: hsla(203.8775510204082, 80.32786885245902%, 52.156862745098046%, 0.5);
  --link-color: #2399E7;
  --text-highlight-color---opacity-high: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.75);
  --text-highlight-color---opacity-higher: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.85);
  --text-highlight-color---opacity-low: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.25);
  --text-highlight-color---opacity-lower: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.1);
  --text-highlight-color---opacity-med: hsla(35.92105263157896, 94.99999999999999%, 68.62745098039215%, 0.5);
  --text-highlight-color: #FBBE63;
  --warning-color---opacity-high: hsla(8.571428571428555, 74.11764705882354%, 50%, 0.75);
  --warning-color---opacity-higher: hsla(8.571428571428555, 74.11764705882354%, 50%, 0.85);
  --warning-color---opacity-low: hsla(8.571428571428555, 74.11764705882354%, 50%, 0.25);
  --warning-color---opacity-lower: hsla(8.571428571428555, 74.11764705882354%, 50%, 0.1);
  --warning-color---opacity-med: hsla(8.571428571428555, 74.11764705882354%, 50%, 0.5);
  --warning-color: #DE3C21;

  background: var(--background-color);
  color: var(--body-text-color);
  font-family: IBM Plex Sans;
}`

export const decorators = [
  (Story) => (
    <>
      <style>{storyStyle}</style>
      <Story />
    </>
  ),
];