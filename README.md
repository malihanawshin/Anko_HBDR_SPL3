# Anko_HBDR_SPL3
Anko (অঙ্ক) is developed as a Bengali handwritten digit as well as number recognizing Android app.
This Android app is created as a tool for recognizing Bengali handwritten digit. It lets users take photo of any handwritten Bangla number. The number can be consisted of one or more digits. For example, it can be a phone number written in Bangla. Data of photo will be uploaded to the server for further processing. NumtaDB Dataset retrieved from Bengali.Ai website was used for training model on Bangla Handwritten Digits. The dataset is a compilation of six datasets that were gathered from different sources and at different times-
i. Bengali Handwritten Digits Database (BHDDB)
ii. BUET101 Database (B101DB)
iii. OngkoDB
iv. ISRTHDB
v. BanglaLekha-Isolated Numerals
vi. UIUDB

Dataset is trained through a bengali digit recognizer model, which is based on a convolutional neural network (CNN). Captured image is matched with the trained dataset and the result is shown in both English digits and words in the app. Tensor Flow and Keras were used as backend. The model was designed using convolution layer, max pooling, flatten, dense, activation softmax and so on. Here, CNN involves more than just one hidden layers, so this neural network falls into Deep Learning.

Randomly 80% of the training data are chosen and used to train the neural network. The remaining 20% images were kept for validation data. These 80% samples (57636 images) are trained in 30 epochs to have a decent training and validation accuracy on dataset. Highest accuracy of 99% were found in different epochs.

Output of the model must be saved in such a format so that it can be used in the android app. Since server side is not built here, the file had to be kept in the project internally. The output was saved using freeze graph in a Protocol Buffer (PB) file format to use with TensorFLow. All of TensorFlow's file formats are based on Protocol Buffers. Protocol Buffers are also referred as protobufs. After defining data structures in text files, the protobuf tools generate classes in C, Python, and other languages that can load, save, and access the data in a friendly way.
