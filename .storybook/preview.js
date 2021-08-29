import React from 'react';
import styled, { css } from 'styled-components';

export const parameters = {
  actions: { argTypesRegex: "^on[A-Z].*" },
  layout: 'fullscreen',
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
}

export const globalTypes = {
  theme: {
    name: 'Theme',
    description: 'Global theme for components',
    defaultValue: 'light',
    toolbar: {
      // icon: 'circlehollow',
      // Array of plain string values or MenuItem shape (see below)
      items: ['light', 'dark'],
      // Property that specifies if the name of the item will be displayed
      showName: true,
    },
  },
};

const themeLight = `
  --background-color---opacity-high: hsla(0, 0%, 96.47058823529412%, 0.75);
  --background-color---opacity-higher: hsla(0, 0%, 96.47058823529412%, 0.85);
  --background-color---opacity-low: hsla(0, 0%, 96.47058823529412%, 0.25);
  --background-color---opacity-lower: hsla(0, 0%, 96.47058823529412%, 0.1);
  --background-color---opacity-med: hsla(0, 0%, 96.47058823529412%, 0.5);
  --background-color: #F6F6F6;
  --background-minus-1---opacity-high: hsla(30, 28.571428571428452%, 97.25490196078431%, 0.75);
  --background-minus-1---opacity-higher: hsla(30, 28.571428571428452%, 97.25490196078431%, 0.85);
  --background-minus-1---opacity-low: hsla(30, 28.571428571428452%, 97.25490196078431%, 0.25);
  --background-minus-1---opacity-lower: hsla(30, 28.571428571428452%, 97.25490196078431%, 0.1);
  --background-minus-1---opacity-med: hsla(30, 28.571428571428452%, 97.25490196078431%, 0.5);
  --background-minus-1: #FAF8F6;
  --background-minus-2---opacity-high: hsla(30.000000000000227, 11.111111111111155%, 92.94117647058823%, 0.75);
  --background-minus-2---opacity-higher: hsla(30.000000000000227, 11.111111111111155%, 92.94117647058823%, 0.85);
  --background-minus-2---opacity-low: hsla(30.000000000000227, 11.111111111111155%, 92.94117647058823%, 0.25);
  --background-minus-2---opacity-lower: hsla(30.000000000000227, 11.111111111111155%, 92.94117647058823%, 0.1);
  --background-minus-2---opacity-med: hsla(30.000000000000227, 11.111111111111155%, 92.94117647058823%, 0.5);
  --background-minus-2: #EFEDEB;
  --background-plus-1---opacity-high: hsla(0, 0%, 98.4313725490196%, 0.75);
  --background-plus-1---opacity-higher: hsla(0, 0%, 98.4313725490196%, 0.85);
  --background-plus-1---opacity-low: hsla(0, 0%, 98.4313725490196%, 0.25);
  --background-plus-1---opacity-lower: hsla(0, 0%, 98.4313725490196%, 0.1);
  --background-plus-1---opacity-med: hsla(0, 0%, 98.4313725490196%, 0.5);
  --background-plus-1: #fbfbfb;
  --background-plus-2---opacity-high: hsla(0, 0%, 100%, 0.75);
  --background-plus-2---opacity-higher: hsla(0, 0%, 100%, 0.85);
  --background-plus-2---opacity-low: hsla(0, 0%, 100%, 0.25);
  --background-plus-2---opacity-lower: hsla(0, 0%, 100%, 0.1);
  --background-plus-2---opacity-med: hsla(0, 0%, 100%, 0.5);
  --background-plus-2: #fff;
  --body-text-color---opacity-high: hsla(38.18181818181819, 8.943089430894311%, 24.11764705882353%, 0.75);
  --body-text-color---opacity-higher: hsla(38.18181818181819, 8.943089430894311%, 24.11764705882353%, 0.85);
  --body-text-color---opacity-low: hsla(38.18181818181819, 8.943089430894311%, 24.11764705882353%, 0.25);
  --body-text-color---opacity-lower: hsla(38.18181818181819, 8.943089430894311%, 24.11764705882353%, 0.1);
  --body-text-color---opacity-med: hsla(38.18181818181819, 8.943089430894311%, 24.11764705882353%, 0.5);
  --body-text-color: #433F38;
  --border-color---opacity-high: hsla(0, 0%, 0%, 0.75);
  --border-color---opacity-higher: hsla(0, 0%, 0%, 0.85);
  --border-color---opacity-low: hsla(0, 0%, 0%, 0.25);
  --border-color---opacity-lower: hsla(0, 0%, 0%, 0.1);
  --border-color---opacity-med: hsla(0, 0%, 0%, 0.5);
  --border-color: hsla(32, 81%, 10%, 0.08);
  --confirmation-color---opacity-high: hsla(133.29113924050637, 100%, 30.980392156862745%, 0.75);
  --confirmation-color---opacity-higher: hsla(133.29113924050637, 100%, 30.980392156862745%, 0.85);
  --confirmation-color---opacity-low: hsla(133.29113924050637, 100%, 30.980392156862745%, 0.25);
  --confirmation-color---opacity-lower: hsla(133.29113924050637, 100%, 30.980392156862745%, 0.1);
  --confirmation-color---opacity-med: hsla(133.29113924050637, 100%, 30.980392156862745%, 0.5);
  --confirmation-color: #009E23;
  --error-color---opacity-high: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.75);
  --error-color---opacity-higher: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.85);
  --error-color---opacity-low: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.25);
  --error-color---opacity-lower: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.1);
  --error-color---opacity-med: hsla(4.838709677419331, 97.89473684210527%, 62.745098039215684%, 0.5);
  --error-color: #fd5243;
  --graph-control-bg---opacity-high: hsla(0, 0%, 97.6470588235294%, 0.75);
  --graph-control-bg---opacity-higher: hsla(0, 0%, 97.6470588235294%, 0.85);
  --graph-control-bg---opacity-low: hsla(0, 0%, 97.6470588235294%, 0.25);
  --graph-control-bg---opacity-lower: hsla(0, 0%, 97.6470588235294%, 0.1);
  --graph-control-bg---opacity-med: hsla(0, 0%, 97.6470588235294%, 0.5);
  --graph-control-bg: #f9f9f9;
  --graph-control-color---opacity-high: hsla(0, 0%, 0%, 0.75);
  --graph-control-color---opacity-higher: hsla(0, 0%, 0%, 0.85);
  --graph-control-color---opacity-low: hsla(0, 0%, 0%, 0.25);
  --graph-control-color---opacity-lower: hsla(0, 0%, 0%, 0.1);
  --graph-control-color---opacity-med: hsla(0, 0%, 0%, 0.5);
  --graph-control-color: black;
  --graph-link-normal---opacity-high: hsla(0, 0%, 81.17647058823529%, 0.75);
  --graph-link-normal---opacity-higher: hsla(0, 0%, 81.17647058823529%, 0.85);
  --graph-link-normal---opacity-low: hsla(0, 0%, 81.17647058823529%, 0.25);
  --graph-link-normal---opacity-lower: hsla(0, 0%, 81.17647058823529%, 0.1);
  --graph-link-normal---opacity-med: hsla(0, 0%, 81.17647058823529%, 0.5);
  --graph-link-normal: #cfcfcf;
  --graph-node-hlt---opacity-high: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.75);
  --graph-node-hlt---opacity-higher: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.85);
  --graph-node-hlt---opacity-low: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.25);
  --graph-node-hlt---opacity-lower: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.1);
  --graph-node-hlt---opacity-med: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.5);
  --graph-node-hlt: #0075E1;
  --graph-node-normal---opacity-high: hsla(0, 0%, 56.470588235294116%, 0.75);
  --graph-node-normal---opacity-higher: hsla(0, 0%, 56.470588235294116%, 0.85);
  --graph-node-normal---opacity-low: hsla(0, 0%, 56.470588235294116%, 0.25);
  --graph-node-normal---opacity-lower: hsla(0, 0%, 56.470588235294116%, 0.1);
  --graph-node-normal---opacity-med: hsla(0, 0%, 56.470588235294116%, 0.5);
  --graph-node-normal: #909090;
  --header-text-color---opacity-high: hsla(260, 8.737864077669899%, 20.19607843137255%, 0.75);
  --header-text-color---opacity-higher: hsla(260, 8.737864077669899%, 20.19607843137255%, 0.85);
  --header-text-color---opacity-low: hsla(260, 8.737864077669899%, 20.19607843137255%, 0.25);
  --header-text-color---opacity-lower: hsla(260, 8.737864077669899%, 20.19607843137255%, 0.1);
  --header-text-color---opacity-med: hsla(260, 8.737864077669899%, 20.19607843137255%, 0.5);
  --header-text-color: #322F38;
  --highlight-color---opacity-high: hsla(33.4673366834171, 94.31279620853081%, 58.62745098039216%, 0.75);
  --highlight-color---opacity-higher: hsla(33.4673366834171, 94.31279620853081%, 58.62745098039216%, 0.85);
  --highlight-color---opacity-low: hsla(33.4673366834171, 94.31279620853081%, 58.62745098039216%, 0.25);
  --highlight-color---opacity-lower: hsla(33.4673366834171, 94.31279620853081%, 58.62745098039216%, 0.1);
  --highlight-color---opacity-med: hsla(33.4673366834171, 94.31279620853081%, 58.62745098039216%, 0.5);
  --highlight-color: #F9A132;
  --link-color---opacity-high: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.75);
  --link-color---opacity-higher: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.85);
  --link-color---opacity-low: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.25);
  --link-color---opacity-lower: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.1);
  --link-color---opacity-med: hsla(208.79999999999995, 100%, 44.11764705882353%, 0.5);
  --link-color: #0075E1;
  --text-highlight-color---opacity-high: hsla(41.53846153846155, 99.99999999999997%, 77.05882352941175%, 0.75);
  --text-highlight-color---opacity-higher: hsla(41.53846153846155, 99.99999999999997%, 77.05882352941175%, 0.85);
  --text-highlight-color---opacity-low: hsla(41.53846153846155, 99.99999999999997%, 77.05882352941175%, 0.25);
  --text-highlight-color---opacity-lower: hsla(41.53846153846155, 99.99999999999997%, 77.05882352941175%, 0.1);
  --text-highlight-color---opacity-med: hsla(41.53846153846155, 99.99999999999997%, 77.05882352941175%, 0.5);
  --text-highlight-color: #ffdb8a;
  --warning-color---opacity-high: hsla(0, 100%, 41.17647058823529%, 0.75);
  --warning-color---opacity-higher: hsla(0, 100%, 41.17647058823529%, 0.85);
  --warning-color---opacity-low: hsla(0, 100%, 41.17647058823529%, 0.25);
  --warning-color---opacity-lower: hsla(0, 100%, 41.17647058823529%, 0.1);
  --warning-color---opacity-med: hsla(0, 100%, 41.17647058823529%, 0.5);
  --warning-color: #D20000;

  --opacity-higher: 0.85;
  --opacity-high: 0.7;
  --opacity-med: 0.5;
  --opacity-low: 0.25;
  --opacity-lower: 0.1;

  --zindex-dropdown: 1000;
  --zindex-sticky: 1020;
  --zindex-fixed: 1030;
  --zindex-modal-backdrop: 1040;
  --zindex-modal: 1050;
  --zindex-popover: 1060;
  --zindex-tooltip: 1070;

  --shadow-color: rgb(0 0 0 / 0.2);

  --depth-shadow-4: 0 2px 4px var(--shadow-color);
  --depth-shadow-8: 0 4px 8px var(--shadow-color);
  --depth-shadow-14: 0 4px 16px var(--shadow-color);
  --depth-shadow-64: 0 24px 60px var(--shadow-color);
`;

const themeDark = `
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

  --opacity-higher: 0.85;
  --opacity-high: 0.7;
  --opacity-med: 0.5;
  --opacity-low: 0.25;
  --opacity-lower: 0.1;

  --opacity-higher: 0.85;
  --opacity-high: 0.7;
  --opacity-med: 0.5;
  --opacity-low: 0.25;
  --opacity-lower: 0.1;

  --zindex-dropdown: 1000;
  --zindex-sticky: 1020;
  --zindex-fixed: 1030;
  --zindex-modal-backdrop: 1040;
  --zindex-modal: 1050;
  --zindex-popover: 1060;
  --zindex-tooltip: 1070;

  --shadow-color: rgb(0 0 0 / 0.2);

  --depth-shadow-4: 0 2px 4px var(--shadow-color);
  --depth-shadow-8: 0 4px 8px var(--shadow-color);
  --depth-shadow-14: 0 4px 16px var(--shadow-color);
  --depth-shadow-64: 0 24px 60px var(--shadow-color);
 }
`;


const BaseStyles = css`
  background-color: var(--background-color);
  font-family: IBM Plex Sans, Sans-Serif;
  color: var(--body-text-color);
  font-size: 16px;
  line-height: 1.5;
  position: relative;
  overflow: hidden;

  &.is-storybook-canvas { }

  ${themeLight}

  &.is-theme-dark {
    ${themeDark}
  }

  * {box-sizing: border-box};
`;

/**
 * Provides contextual classnames, colors, and typographic
 * defaults which all components expect.
 * 
 * Not used in the application, but useful for testing.
 */
const App = styled.div`
  ${BaseStyles}
`;

export const decorators = [
  (Story, context) => {
    console.log(context);
    return (
      <App
        id="app"
        className={[
        context.globals.theme === 'light' ? 'is-theme-light' : 'is-theme-dark',
        context.globals.hostType === 'electron' ? 'is-electron' : 'is-browser',
        context.viewMode === 'docs' ? 'is-storybook-docs' : 'is-storybook-canvas'
      ].join(' ')}>
        <Story />
      </App>
    )
  },
];